package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Activities

/**
 * 活动配置业务服务。
 */
interface ActivitiesService {
    /** 查询全部活动。 */
    fun findAll(): List<Activities>

    /** 根据活动主键查询活动，不存在时抛出业务异常。 */
    fun findById(id: Int): Activities

    /** 根据活动访问地址查询活动，不存在时抛出业务异常。 */
    fun findByUrl(url: String): Activities

    /** 查询所有已开放活动，并按开始时间排序。 */
    fun findOpenActivities(): List<Activities>

    /** 创建活动，并同步绑定活动下属日程和车辆关系。 */
    fun create(activity: Activities): Activities

    /** 更新指定活动，并同步更新活动下属日程和车辆关系。 */
    fun update(id: Int, activity: Activities): Activities

    /** 删除指定活动。 */
    fun delete(id: Int)
}
