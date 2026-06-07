package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 活动车辆数据仓库。
 */
interface CarRepository : JpaRepository<Car, Int> {
    /**
     * 查询指定活动下的全部车辆。
     */
    fun findByActivityId(activityId: Int): List<Car>

    /**
     * 查询指定活动下的指定车号车辆。
     */
    fun findByActivityIdAndCarNumber(activityId: Int, carNumber: Long): Car?
}
