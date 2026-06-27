package top.foxball.receptionbackendsystem.service.impl

import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.controller.request.InspectionTeamItemDto
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItinerary
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.InspectionTeamService

/**
 * 考察组服务实现，操作 [InspectionTeamItem] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的考察组集合，并级联重建每组下的天级行程（含路线节点、活动安排），
 * 借助活动实体的 orphanRemoval 级联清理被移除的考察组与行程。
 */
@Service
class InspectionTeamServiceImpl(
    private val inspectionTeamRepository: InspectionTeamRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<InspectionTeamItem, Long>(inspectionTeamRepository), InspectionTeamService {
    /**
     * 按活动查询其下全部考察组。
     *
     * 同时预初始化每组的天级行程及其路线节点、活动安排集合，避免序列化阶段触发懒加载异常。
     */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<InspectionTeamItem> {
        val teams = inspectionTeamRepository.findByActivityId(activityId)
        // 预加载各天行程与子集合，避免序列化时懒加载异常。
        teams.forEach { team ->
            Hibernate.initialize(team.itineraries)
            team.itineraries.forEach { itinerary ->
                Hibernate.initialize(itinerary.routeNode)
                Hibernate.initialize(itinerary.eventArrangements)
            }
        }
        return teams
    }

    /** 按名称关键字模糊匹配考察组。 */
    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<InspectionTeamItem> =
        inspectionTeamRepository.findByNameContaining(name)

    /**
     * 整体覆盖保存某活动的考察组及其天级行程。
     *
     * 步骤：
     * 1) 加载活动，按 id 建立既有考察组索引与既有日程（天）索引（行程须挂在本活动已存在的天之下）；
     * 2) 逐组归一化：命中 id 复用既有考察组做更新，否则新建，并回填活动引用与组名；
     * 3) 在组内逐条重建行程：按 id 复用既有行程，否则新建；路线节点与活动安排整体清空后按请求深拷贝重建；
     *    缺失 scheduleId 或对应天不存在的行程被跳过；未出现在请求中的既有行程由 orphanRemoval 清理；
     * 4) 清空 [Activities.inspectionTeamItemList] 后整体替换为归一化结果并保存活动。
     */
    @Transactional
    override fun saveByActivity(activityId: Long, groups: List<InspectionTeamItemDto>): List<InspectionTeamItem> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")

        val existingTeamsById = activity.inspectionTeamItemList.mapNotNull { team ->
            team.id?.let { it to team }
        }.toMap()
        val scheduleById = activity.schedules.mapNotNull { schedule ->
            schedule.id?.let { it to schedule }
        }.toMap()

        val normalizedGroups = groups.map { group ->
            val targetTeam = group.id?.let(existingTeamsById::get) ?: InspectionTeamItem()
            targetTeam.activity = activity
            targetTeam.name = group.name

            // 既有行程按 id 匹配，重建为请求中的行程；未出现的行程由 orphanRemoval 清理。
            val existingItinerariesById = targetTeam.itineraries.mapNotNull { it.id?.let { id -> id to it } }.toMap()
            val normalizedItineraries = group.itineraries.mapNotNull { itinerary ->
                val scheduleId = itinerary.scheduleId
                    ?: return@mapNotNull null
                val targetSchedule = scheduleById[scheduleId]
                    ?: return@mapNotNull null // 行程必须挂在本活动下已存在的天

                val targetItinerary = itinerary.id?.let(existingItinerariesById::get) ?: InspectionTeamItinerary()
                targetItinerary.inspectionTeam = targetTeam
                targetItinerary.schedule = targetSchedule
                targetItinerary.routeUrl = itinerary.routeUrl
                targetItinerary.scheduleUrl = itinerary.scheduleUrl
                targetItinerary.routeNode.clear()
                targetItinerary.routeNode.addAll(itinerary.routeNode)
                targetItinerary.eventArrangements.clear()
                targetItinerary.eventArrangements.addAll(itinerary.eventArrangements.map { it.copy() })
                targetItinerary
            }

            targetTeam.itineraries.clear()
            targetTeam.itineraries.addAll(normalizedItineraries)
            targetTeam
        }

        activity.inspectionTeamItemList.clear()
        activity.inspectionTeamItemList.addAll(normalizedGroups)

        return activitiesRepository.saveAndFlush(activity).inspectionTeamItemList
    }
}
