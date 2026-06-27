package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

/**
 * 颜色标签业务服务契约。
 *
 * 为 [ColorTag] 提供按活动维度查询、按名称/类型组合检索、存在性判定
 * 以及按活动整体覆盖保存标签列表。
 */
interface ColorTagService : ActivityEntityService<ColorTag, Int> {
    /**
     * 整体覆盖保存活动下的颜色标签列表。
     *
     * 以 [activityId] 为维度用 [colorTags] 覆盖该活动原有标签。
     */
    fun saveByActivity(activityId: Long, colorTags: List<ColorTag>): List<ColorTag>

    /** 返回指定活动下指定类型的全部颜色标签。 */
    fun findByActivityIdAndType(activityId: Long, type: String): List<ColorTag>

    /** 返回指定活动下指定名称的颜色标签，不存在时返回 null。 */
    fun findByActivityIdAndName(activityId: Long, name: String): ColorTag?

    /** 返回指定活动下指定名称与类型组合的颜色标签，不存在时返回 null。 */
    fun findByActivityIdAndNameAndType(activityId: Long, name: String, type: String): ColorTag?

    /** 判断指定活动下是否存在同名颜色标签。 */
    fun existsByActivityIdAndName(activityId: Long, name: String): Boolean

    /** 判断指定活动下是否存在同名称、同类型的颜色标签。 */
    fun existsByActivityIdAndNameAndType(activityId: Long, name: String, type: String): Boolean
}
