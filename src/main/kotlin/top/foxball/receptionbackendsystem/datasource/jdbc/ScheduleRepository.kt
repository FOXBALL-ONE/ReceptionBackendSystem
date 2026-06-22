package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 活动日程数据仓库。
 */
interface ScheduleRepository : ReceptionRepository<Schedule, Long> {
    /**
     * 查询指定活动下的全部日程。
     */
    fun findByActivityId(activityId: Int): List<Schedule>

    /**
     * 根据日程标签模糊查询。
     */
    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>
}
