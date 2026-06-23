package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 颜色标签数据仓库。
 */
interface ColorTagRepository : ReceptionRepository<ColorTag, Int> {
    /**
     * 查询指定活动下的全部颜色标签。
     */
    fun findByActivityId(activityId: Long): List<ColorTag>

    /**
     * 查询指定活动下指定用途类型的颜色标签。
     */
    fun findByActivityIdAndType(activityId: Long, type: String): List<ColorTag>

    /**
     * 根据标签名称查询颜色标签。
     */
    fun findByName(name: String): ColorTag?

    /**
     * 查询指定活动下的指定颜色标签；未指定用途类型时只返回第一条以兼容旧接口。
     */
    fun findFirstByActivityIdAndNameOrderByIdAsc(activityId: Long, name: String): ColorTag?

    /**
     * 查询指定活动下指定用途类型和名称的颜色标签。
     */
    fun findByActivityIdAndNameAndType(activityId: Long, name: String, type: String): ColorTag?

    /**
     * 判断指定标签名称是否已存在。
     */
    fun existsByName(name: String): Boolean

    /**
     * 判断指定活动下的标签名称是否已存在。
     */
    fun existsByActivityIdAndName(activityId: Long, name: String): Boolean

    /**
     * 判断指定活动下指定用途类型的标签名称是否已存在。
     */
    fun existsByActivityIdAndNameAndType(activityId: Long, name: String, type: String): Boolean
}
