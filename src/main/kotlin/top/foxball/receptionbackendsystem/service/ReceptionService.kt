package top.foxball.receptionbackendsystem.service

/**
 * 接待系统 CRUD 服务根接口。
 *
 * 为实体类型 [T]（主键类型 [ID]）定义统一的单条/批量保存、更新、删除与按主键查询能力，
 * 是各业务服务（活动、人员、车辆等）共同实现的契约。
 */
interface ReceptionService<T : Any, ID : Any> {
    /** 保存单条实体并返回持久化结果。 */
    fun saveOne(entity: T): T

    /** 批量保存实体并返回持久化结果列表。 */
    fun saveBatch(entities: Iterable<T>): List<T>

    /** 更新单条实体并返回更新后的结果。 */
    fun updateOne(entity: T): T

    /** 批量更新实体并返回更新后的结果列表。 */
    fun updateBatch(entities: Iterable<T>): List<T>

    /** 删除单条实体。 */
    fun deleteOne(entity: T)

    /** 批量删除实体。 */
    fun deleteBatch(entities: Iterable<T>)

    /** 按主键查询实体，不存在时返回 null。 */
    fun findEntityById(id: ID): T?
}
