package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem

/**
 * 考察组安排业务服务。
 */
interface InspectionTeamItemService {
    /** 查询全部考察组安排。 */
    fun findAll(): List<InspectionTeamItem>

    /** 根据主键查询考察组安排，不存在时抛出业务异常。 */
    fun findById(id: Long): InspectionTeamItem

    /** 根据考察组名称模糊查询安排。 */
    fun findByNameContaining(name: String): List<InspectionTeamItem>

    /** 创建考察组安排。 */
    fun create(inspectionTeamItem: InspectionTeamItem): InspectionTeamItem

    /** 更新考察组安排。 */
    fun update(id: Long, inspectionTeamItem: InspectionTeamItem): InspectionTeamItem

    /** 删除考察组安排。 */
    fun delete(id: Long)
}
