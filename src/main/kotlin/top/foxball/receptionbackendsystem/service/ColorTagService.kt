package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

/**
 * 颜色标签业务服务。
 */
interface ColorTagService {
    /** 查询全部颜色标签。 */
    fun findAll(): List<ColorTag>

    /** 根据颜色标签主键查询标签，不存在时抛出业务异常。 */
    fun findById(id: Int): ColorTag

    /** 根据标签名称查询颜色标签。 */
    fun findByName(name: String): ColorTag

    /** 创建颜色标签。 */
    fun create(colorTag: ColorTag): ColorTag

    /** 更新颜色标签。 */
    fun update(id: Int, colorTag: ColorTag): ColorTag

    /** 删除颜色标签。 */
    fun delete(id: Int)
}
