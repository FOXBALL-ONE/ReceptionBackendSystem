package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 考察组安排数据仓库。
 */
interface InspectionTeamRepository : JpaRepository<InspectionTeamItem, Long> {
    /**
     * 根据考察组名称模糊查询。
     */
    fun findByNameContaining(name: String): List<InspectionTeamItem>
}
