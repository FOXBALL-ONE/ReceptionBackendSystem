package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.ScheduleRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ScheduleService

/**
 * 活动日程（天）服务实现，操作 [Schedule] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的天列表：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的天及其下游（考察组行程等以天为单位挂载）。
 */
@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Schedule, Long>(scheduleRepository), ScheduleService {
    /** 按活动查询其下全部日程（天）。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Schedule> =
        scheduleRepository.findByActivityId(activityId)

    /** 按日程标签关键字模糊匹配日程列表。 */
    @Transactional(readOnly = true)
    override fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule> =
        scheduleRepository.findByScheduleTagsContaining(scheduleTags)

    /**
     * 整体覆盖保存某活动的日程（天）列表。
     *
     * 步骤：1) 加载活动并按 id 建立既有日程索引；
     * 2) 逐项归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与标签；
     * 3) 清空 [Activities.schedules] 后整体替换为归一化结果并保存活动。
     */
    @Transactional
    override fun saveByActivity(activityId: Long, schedules: List<Schedule>): List<Schedule> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingSchedulesById = activity.schedules.mapNotNull { schedule ->
            schedule.id?.let { it to schedule }
        }.toMap()

        // 按 id 匹配既有天做更新/新增；未出现在请求中的天被整体移除（orphanRemoval 兜底）。
        val normalizedSchedules = schedules.map { schedule ->
            val targetSchedule = schedule.id?.let(existingSchedulesById::get) ?: Schedule()
            targetSchedule.activity = activity
            targetSchedule.scheduleTags = schedule.scheduleTags
            targetSchedule
        }

        activity.schedules.clear()
        activity.schedules.addAll(normalizedSchedules)

        return activitiesRepository.saveAndFlush(activity).schedules
    }
}
