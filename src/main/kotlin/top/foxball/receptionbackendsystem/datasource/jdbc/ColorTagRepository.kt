package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 颜色标签数据仓库。
 */
interface ColorTagRepository : ReceptionRepository<ColorTag, Int> {
    /**
     * 查询指定活动下的全部颜色标签。
     */
    fun findByActivityId(activityId: Int): List<ColorTag>

    /**
     * 根据标签名称查询颜色标签。
     */
    fun findByName(name: String): ColorTag?

    /**
     * 查询指定活动下的指定颜色标签。
     */
    fun findByActivityIdAndName(activityId: Int, name: String): ColorTag?

    /**
     * 判断指定标签名称是否已存在。
     */
    fun existsByName(name: String): Boolean

    /**
     * 判断指定活动下的标签名称是否已存在。
     */
    fun existsByActivityIdAndName(activityId: Int, name: String): Boolean
}
