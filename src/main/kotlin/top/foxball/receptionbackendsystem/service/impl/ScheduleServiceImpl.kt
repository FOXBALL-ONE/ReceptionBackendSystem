package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.ScheduleRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ScheduleService

@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Schedule, Long>(scheduleRepository), ScheduleService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Schedule> =
        scheduleRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule> =
        scheduleRepository.findByScheduleTagsContaining(scheduleTags)

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
