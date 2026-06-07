package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 活动配置数据仓库。
 */
interface ActivitiesRepository : JpaRepository<Activities, Int> {
    /**
     * 根据活动访问地址查询活动。
     */
    fun findByUrl(url: String): Activities?

    /**
     * 判断指定访问地址是否已存在。
     */
    fun existsByUrl(url: String): Boolean

    /**
     * 查询所有已开放活动，并按开始时间升序排序。
     */
    fun findByIsOpenTrueOrderByStartTimeAsc(): List<Activities>
}
