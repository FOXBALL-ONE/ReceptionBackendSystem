package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Person

/**
 * 活动人员业务服务契约。
 *
 * 为 [Person] 提供按活动维度查询、按姓名/单位筛选以及按活动整体覆盖保存人员名单。
 */
interface PersonService : ActivityEntityService<Person, Int> {
    /** 按姓名模糊匹配人员。 */
    fun findByNameContaining(name: String): List<Person>

    /** 返回指定活动下某单位的全部人员。 */
    fun findByActivityIdAndUnit(activityId: Long, unit: String): List<Person>

    /**
     * 整体覆盖保存活动下的人员列表。
     *
     * 以 [activityId] 为维度用 [persons] 覆盖该活动原有人员：
     * 匹配既有记录做更新，新增未出现的，删除未出现的。
     */
    fun saveByActivity(activityId: Long, persons: List<Person>): List<Person>
}
