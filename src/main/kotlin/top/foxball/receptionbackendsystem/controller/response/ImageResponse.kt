package top.foxball.receptionbackendsystem.controller.response

import top.foxball.receptionbackendsystem.config.ImageProperties
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import java.time.LocalDateTime

/**
 * 图片接口响应对象。
 *
 * 与数据库实体 [Image] 的区别是：数据库只保存相对路径，
 * 这里的 [accessUrl] 会被转换为前端可直接访问的完整公开 URL。
 */
data class ImageResponse(
    /** 图片元数据主键。 */
    val id: Long?,

    /** 上传时的原始文件名。 */
    val originalFilename: String,

    /** 服务端保存时生成的文件名。 */
    val storedFilename: String,

    /** 文件扩展名，例如 jpg、png、webp。 */
    val extension: String?,

    /** 文件 MIME 类型。 */
    val contentType: String,

    /** 文件大小，单位字节。 */
    val fileSize: Long,

    /** 图片宽度，单位像素。 */
    val width: Int?,

    /** 图片高度，单位像素。 */
    val height: Int?,

    /** 文件 SHA-256 摘要。 */
    val sha256: String?,

    /** 兼容对象存储场景的桶名称或存储服务名称。 */
    val bucket: String?,

    /** 图片相对路径，用于按对象键查询。 */
    val objectKey: String,

    /** 图片相对存储路径，数据库中保存的就是这个形式。 */
    val storagePath: String,

    /** 前端可直接访问的公开 URL。 */
    val accessUrl: String,

    /** 上传人标识。 */
    val uploadedBy: String?,

    /** 图片用途或业务分组。 */
    val usageType: String?,

    /** 是否已软删除。 */
    val isDeleted: Boolean,

    /** 创建时间。 */
    val createdAt: LocalDateTime?,

    /** 更新时间。 */
    val updatedAt: LocalDateTime?,
) {
    companion object {
        /**
         * 将数据库实体转换成接口响应。
         *
         * 转换过程中会再次规范化相对路径，避免历史数据中残留公开 URL 或绝对路径。
         */
        fun from(image: Image, imageProperties: ImageProperties): ImageResponse {
            val relativePath = imageProperties.normalizeRelativePath(
                image.storagePath.ifBlank { image.objectKey.ifBlank { image.accessUrl } }
            )
            return ImageResponse(
                id = image.id,
                originalFilename = image.originalFilename,
                storedFilename = image.storedFilename,
                extension = image.extension,
                contentType = image.contentType,
                fileSize = image.fileSize,
                width = image.width,
                height = image.height,
                sha256 = image.sha256,
                bucket = image.bucket,
                objectKey = relativePath,
                storagePath = relativePath,
                accessUrl = imageProperties.accessUrl(relativePath),
                uploadedBy = image.uploadedBy,
                usageType = image.usageType,
                isDeleted = image.isDeleted,
                createdAt = image.createdAt,
                updatedAt = image.updatedAt,
            )
        }
    }
}
