package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Car

/**
 * 车辆业务服务契约。
 *
 * 为 [Car] 提供按活动维度查询、按车号精确查询以及整体覆盖保存车辆列表。
 */
interface CarService : ActivityEntityService<Car, Int> {
    /** 返回指定活动下指定车号的车辆，不存在时返回 null。 */
    fun findByActivityIdAndCarNumber(activityId: Long, carNumber: Long): Car?

    /**
     * 整体覆盖保存活动下的车辆列表。
     *
     * 以 [activityId] 为维度用 [cars] 覆盖该活动原有车辆。
     */
    fun saveByActivity(activityId: Long, cars: List<Car>): List<Car>
}
