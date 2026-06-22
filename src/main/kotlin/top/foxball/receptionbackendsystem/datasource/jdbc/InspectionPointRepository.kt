package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 考察点数据仓库。
 */
interface InspectionPointRepository : ReceptionRepository<InspectionPoint, Int> {
    /**
     * 查询指定活动下的全部考察点。
     */
    fun findByActivityId(activityId: Int): List<InspectionPoint>
}
