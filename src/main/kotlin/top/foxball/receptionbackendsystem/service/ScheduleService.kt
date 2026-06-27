package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

/**
 * 日程业务服务契约。
 *
 * 为 [Schedule] 提供按活动维度查询、按标签模糊匹配以及按活动整体覆盖保存日程天列表。
 * 日程天作为各活动下"天注册表"，是考察组行程按天挂载的权威来源。
 */
interface ScheduleService : ActivityEntityService<Schedule, Long> {
    /**
     * 整体保存活动下的日程天列表（天注册表）。
     *
     * 按 id 匹配既有天进行更新/新增；请求中未出现的天将被删除。
     */
    fun saveByActivity(activityId: Long, schedules: List<Schedule>): List<Schedule>

    /** 按日程标签文案模糊匹配日程天。 */
    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>
}
