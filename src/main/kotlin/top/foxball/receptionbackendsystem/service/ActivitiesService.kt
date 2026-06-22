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

    /** 关闭活动。 */
    fun closeActivity(id: Int): Activities

    /** 开放活动。 */
    fun openActivity(id: Int): Activities

    /** 搜索活动。 */
    fun searchActivities(keyword: String?, status: String?): List<Activities>

    /** 获取活动统计信息。 */
    fun getStatistics(): Map<String, Any>

    /** 批量删除活动。 */
    fun batchDelete(ids: List<Int>): Map<String, Any>

    /** 复制活动。 */
    fun duplicateActivity(id: Int, newUrl: String, newName: String?): Activities
}
