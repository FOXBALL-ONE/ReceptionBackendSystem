package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 活动车辆数据仓库。
 */
interface CarRepository : ReceptionRepository<Car, Int> {
    /**
     * 查询指定活动下的全部车辆。
     */
    fun findByActivityId(activityId: Long): List<Car>

    /**
     * 查询指定活动下的指定车号车辆。
     */
    fun findByActivityIdAndCarNumber(activityId: Long, carNumber: Long): Car?
}
