package top.foxball.receptionbackendsystem.service

import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.controller.response.ImageResponse
import top.foxball.receptionbackendsystem.datasource.jdbc.Image

/**
 * 图片文件元数据业务服务。
 */
interface ImageService {
    /** 查询全部未删除图片元数据。 */
    fun findAll(): List<ImageResponse>

    /** 根据图片主键查询未删除图片元数据，不存在时抛出业务异常。 */
    fun findById(id: Long): ImageResponse

    /** 根据对象键查询未删除图片元数据。 */
    fun findByObjectKey(objectKey: String): ImageResponse

    /** 根据 SHA-256 摘要查询未删除图片元数据。 */
    fun findBySha256(sha256: String): ImageResponse

    /** 根据用途类型查询未删除图片元数据。 */
    fun findByUsageType(usageType: String): List<ImageResponse>

    /** 创建图片元数据。 */
    fun create(image: Image): ImageResponse

    /** 更新图片元数据，保留创建时间和删除状态。 */
    fun update(id: Long, image: Image): ImageResponse

    /** 上传图片文件并创建元数据。 */
    fun upload(file: MultipartFile, usageType: String?, uploadedBy: String?): ImageResponse

    /** 软删除图片元数据。 */
    fun delete(id: Long)
}
