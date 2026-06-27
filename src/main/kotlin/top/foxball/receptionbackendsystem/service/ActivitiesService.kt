package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Activities

/**
 * 活动业务服务契约。
 *
 * 直接继承 [ReceptionService]（活动本身不隶属于另一个活动），
 * 在其 CRUD 基础上提供活动列表、按 ID/URL 查询、对外开放状态管理
 * 以及活动基础信息（标题、Banner、时间等）的保存与更新。
 */
interface ActivitiesService : ReceptionService<Activities, Long> {
    /** 返回全部活动。 */
    fun findAllActivities(): List<Activities>

    /** 按主键查询活动，不存在时返回 null。 */
    fun findByActivityId(activityId: Long): Activities?

    /** 按访问地址/路由地址查询活动，不存在时返回 null。 */
    fun findByUrl(url: String): Activities?

    /** 判断指定 URL 是否已存在活动。 */
    fun existsByUrl(url: String): Boolean

    /** 返回所有对外开放的活动。 */
    fun findOpenActivities(): List<Activities>

    /** 更新活动的对外开放状态并返回更新后的活动。 */
    fun updateIsOpen(id: Long, isOpen: Boolean): Activities

    /** 保存活动的基础信息（标题、Banner、时间等）并返回持久化结果。 */
    fun saveBasic(activity: Activities): Activities

    /** 更新指定活动的基础信息并返回更新后的活动。 */
    fun updateBasic(id: Long, activity: Activities): Activities
}
