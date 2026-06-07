package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Person

/**
 * 活动人员业务服务。
 */
interface PersonService {
    /** 查询全部人员。 */
    fun findAll(): List<Person>

    /** 根据人员主键查询人员，不存在时抛出业务异常。 */
    fun findById(id: Int): Person

    /** 查询指定活动下的全部人员。 */
    fun findByActivityId(activityId: Int): List<Person>

    /** 根据人员姓名模糊查询。 */
    fun findByNameContaining(name: String): List<Person>

    /** 查询指定活动下指定单位的人员。 */
    fun findByActivityIdAndUnit(activityId: Int, unit: String): List<Person>

    /** 在指定活动下创建人员。 */
    fun create(activityId: Int, person: Person): Person

    /** 更新人员信息。 */
    fun update(id: Int, person: Person): Person

    /** 删除指定人员。 */
    fun delete(id: Int)
}
