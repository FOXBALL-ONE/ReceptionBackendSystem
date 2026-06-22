package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

interface ScheduleService : ActivityEntityService<Schedule, Long> {
    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>

    fun saveByActivity(activityId: Long, schedules: List<Schedule>): List<Schedule>
}
