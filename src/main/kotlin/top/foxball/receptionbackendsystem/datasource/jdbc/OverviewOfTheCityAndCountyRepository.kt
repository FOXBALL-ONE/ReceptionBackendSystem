package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 市县概况数据仓库。
 */
interface OverviewOfTheCityAndCountyRepository : ReceptionRepository<OverviewOfTheCityAndCounty, Int> {
    /**
     * 查询指定活动下的全部市县概况。
     */
    fun findByActivityId(activityId: Long): List<OverviewOfTheCityAndCounty>
}
