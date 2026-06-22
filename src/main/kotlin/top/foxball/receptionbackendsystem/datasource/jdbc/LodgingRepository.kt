package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 住宿安排数据仓库。
 */
interface LodgingRepository : ReceptionRepository<Lodging, Int> {
    /**
     * 查询指定活动下的全部住宿安排。
     */
    fun findByActivityId(activityId: Int): List<Lodging>
}
