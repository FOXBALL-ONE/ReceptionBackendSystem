package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

interface ScheduleService : ActivityEntityService<Schedule, Long> {
    /**
     * 整体保存活动下的日程天列表（天注册表）。
     *
     * 按 id 匹配既有天进行更新/新增；请求中未出现的天将被删除。
     */
    fun saveByActivity(activityId: Long, schedules: List<Schedule>): List<Schedule>

    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>
}
