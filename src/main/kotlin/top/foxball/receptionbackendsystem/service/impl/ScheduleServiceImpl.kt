package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.ScheduleRepository
import top.foxball.receptionbackendsystem.service.ScheduleService

@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
) : AbstractReceptionService<Schedule, Long>(scheduleRepository), ScheduleService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Schedule> =
        scheduleRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule> =
        scheduleRepository.findByScheduleTagsContaining(scheduleTags)
}
