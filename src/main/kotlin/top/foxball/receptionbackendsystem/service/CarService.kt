package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Car

/**
 * 活动车辆业务服务。
 */
interface CarService {
    /** 查询全部车辆。 */
    fun findAll(): List<Car>

    /** 根据车辆主键查询车辆，不存在时抛出业务异常。 */
    fun findById(id: Int): Car

    /** 查询指定活动下的全部车辆。 */
    fun findByActivityId(activityId: Int): List<Car>

    /** 查询指定活动下的指定车号车辆。 */
    fun findByActivityIdAndCarNumber(activityId: Int, carNumber: Long): Car

    /** 在指定活动下创建车辆。 */
    fun create(activityId: Int, car: Car): Car

    /** 更新车辆信息。 */
    fun update(id: Int, car: Car): Car

    /** 删除指定车辆。 */
    fun delete(id: Int)

    /** 批量保存车辆，先删除活动下的旧车辆，再批量插入新车辆。 */
    fun batchSave(activityId: Int, cars: List<Car>): List<Car>
}
