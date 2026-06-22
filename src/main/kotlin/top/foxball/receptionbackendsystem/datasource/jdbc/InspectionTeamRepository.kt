package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 考察组安排数据仓库。
 */
interface InspectionTeamRepository : ReceptionRepository<InspectionTeamItem, Long> {
    /**
     * 查询指定活动下的全部考察组安排。
     */
    fun findByActivityId(activityId: Int): List<InspectionTeamItem>

    /**
     * 根据考察组名称模糊查询。
     */
    fun findByNameContaining(name: String): List<InspectionTeamItem>
}
