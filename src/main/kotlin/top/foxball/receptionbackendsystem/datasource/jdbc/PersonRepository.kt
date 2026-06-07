package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 活动人员数据仓库。
 */
interface PersonRepository : JpaRepository<Person, Int> {
    /**
     * 查询指定活动下的全部人员。
     */
    fun findByActivityId(activityId: Int): List<Person>

    /**
     * 根据人员姓名模糊查询。
     */
    fun findByNameContaining(name: String): List<Person>

    /**
     * 查询指定活动下指定单位的人员。
     */
    fun findByActivityIdAndUnit(activityId: Int, unit: String): List<Person>
}
