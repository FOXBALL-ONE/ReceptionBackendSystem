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

@Service
class InspectionTeamServiceImpl(
    private val inspectionTeamRepository: InspectionTeamRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<InspectionTeamItem, Long>(inspectionTeamRepository), InspectionTeamService {
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

    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<InspectionTeamItem> =
        inspectionTeamRepository.findByNameContaining(name)

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
