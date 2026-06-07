package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Image

/**
 * 图片文件元数据业务服务。
 */
interface ImageService {
    /** 查询全部未删除图片元数据。 */
    fun findAll(): List<Image>

    /** 根据图片主键查询未删除图片元数据，不存在时抛出业务异常。 */
    fun findById(id: Long): Image

    /** 根据对象键查询未删除图片元数据。 */
    fun findByObjectKey(objectKey: String): Image

    /** 根据 SHA-256 摘要查询未删除图片元数据。 */
    fun findBySha256(sha256: String): Image

    /** 根据用途类型查询未删除图片元数据。 */
    fun findByUsageType(usageType: String): List<Image>

    /** 创建图片元数据。 */
    fun create(image: Image): Image

    /** 更新图片元数据，保留创建时间和删除状态。 */
    fun update(id: Long, image: Image): Image

    /** 软删除图片元数据。 */
    fun delete(id: Long)
}
