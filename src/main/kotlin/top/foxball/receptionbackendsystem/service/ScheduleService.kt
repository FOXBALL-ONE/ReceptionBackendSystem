package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

interface ScheduleService : ActivityEntityService<Schedule, Long> {
    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>
}
