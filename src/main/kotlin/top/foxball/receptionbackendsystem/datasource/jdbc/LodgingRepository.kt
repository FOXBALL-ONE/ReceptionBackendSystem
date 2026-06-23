package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 住宿安排数据仓库。
 */
interface LodgingRepository : ReceptionRepository<Lodging, Int> {
    /**
     * 查询指定活动下的全部住宿安排。
     */
    fun findByActivityId(activityId: Long): List<Lodging>

    /**
     * 查询引用指定颜色标签的住宿安排。
     */
    fun findByColorTagId(colorTagId: Int): List<Lodging>
}
