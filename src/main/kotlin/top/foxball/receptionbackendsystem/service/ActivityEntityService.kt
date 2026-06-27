package top.foxball.receptionbackendsystem.service

/**
 * 按活动维度组织实体的服务根接口。
 *
 * 在 [ReceptionService] 的基础上为实体类型 [T]（主键类型 [ID]）增加按活动查询能力，
 * 供所有"隶属于某个活动"的实体（人员、车辆、日程等）统一实现。
 */
interface ActivityEntityService<T : Any, ID : Any> : ReceptionService<T, ID> {
    /** 返回指定活动下的全部实体。 */
    fun findByActivityId(activityId: Long): List<T>
}
