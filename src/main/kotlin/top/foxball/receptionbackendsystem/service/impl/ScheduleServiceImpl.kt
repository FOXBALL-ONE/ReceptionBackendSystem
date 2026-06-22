package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.ScheduleRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ScheduleService

/**
 * 活动日程业务服务实现。
 */
@Service
@Transactional
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
    private val activitiesRepository: ActivitiesRepository,
) : ScheduleService {

    /** 查询全部日程。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<Schedule> = scheduleRepository.findAll()

    /** 根据主键查询日程。 */
    @Transactional(readOnly = true)
    override fun findById(id: Long): Schedule = scheduleRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("日程不存在：$id") }

    /** 查询指定活动下的日程列表。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Schedule> = scheduleRepository.findByActivityId(activityId)

    /** 根据日程标签模糊查询。 */
    @Transactional(readOnly = true)
    override fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule> =
        scheduleRepository.findByScheduleTagsContaining(scheduleTags)

    /** 在指定活动下创建日程。 */
    override fun create(activityId: Int, schedule: Schedule): Schedule {
        schedule.id = null
        schedule.activity = findActivity(activityId)
        return scheduleRepository.save(schedule)
    }

    /** 更新日程信息，并保留或重设所属活动关系。 */
    override fun update(id: Long, schedule: Schedule): Schedule {
        val existing = findById(id)
        val activity = schedule.activity?.id?.let(::findActivity)
            ?: existing.activity
            ?: throw ResourceNotFoundException("日程所属活动不存在")

        schedule.id = id
        schedule.activity = activity
        return scheduleRepository.save(schedule)
    }

    /** 删除日程。 */
    override fun delete(id: Long) {
        if (!scheduleRepository.existsById(id)) {
            throw ResourceNotFoundException("日程不存在：$id")
        }

        scheduleRepository.deleteById(id)
    }

    /** 批量保存日程，先删除活动下的旧日程，再批量插入新日程。 */
    override fun batchSave(activityId: Int, schedules: List<Schedule>): List<Schedule> {
        val activity = findActivity(activityId)

        // 删除该活动下的所有旧日程
        val oldSchedules = scheduleRepository.findByActivityId(activityId)
        scheduleRepository.deleteAll(oldSchedules)

        // 批量插入新日程
        val newSchedules = schedules.map { schedule ->
            schedule.id = null
            schedule.activity = activity

            // 绑定考察组到活动
            schedule.inspectionTeamItem.forEach { team ->
                team.activity = activity
            }

            schedule
        }

        return scheduleRepository.saveAll(newSchedules)
    }

    /** 查询日程所属活动。 */
    private fun findActivity(activityId: Int): Activities = activitiesRepository.findById(activityId)
        .orElseThrow { ResourceNotFoundException("活动不存在：$activityId") }
}
