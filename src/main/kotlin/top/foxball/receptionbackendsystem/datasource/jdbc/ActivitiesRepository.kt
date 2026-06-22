package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 活动配置数据仓库。
 */
interface ActivitiesRepository : ReceptionRepository<Activities, Long> {
    fun findAllByOrderByStartTimeAsc(): List<Activities>

    /**
     * 根据活动 ID 查询活动。
     */
    fun findByActivityId(activityId: Long): Activities? = findEntityById(activityId)

    /**
     * 根据活动访问地址查询活动。
     */
    fun findByUrl(url: String): Activities?

    /**
     * 判断指定访问地址是否已存在。
     */
    fun existsByUrl(url: String): Boolean

    /**
     * 查询所有已开放活动，并按开始时间升序排序。
     */
    fun findByIsOpenTrueOrderByStartTimeAsc(): List<Activities>
}
