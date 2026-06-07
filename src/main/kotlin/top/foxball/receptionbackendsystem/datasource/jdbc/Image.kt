package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 图片文件元数据实体。
 *
 * 只保存图片文件的描述信息和存储定位信息，不保存图片二进制内容。
 */
@Table(
    name = "images",
    indexes = [
        Index(name = "idx_images_sha256", columnList = "sha256"),
        Index(name = "idx_images_object_key", columnList = "object_key"),
        Index(name = "idx_images_created_at", columnList = "created_at"),
    ]
)
@Entity
data class Image(
    /** 图片元数据主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 上传时的原始文件名。 */
    @Column(name = "original_filename", nullable = false, length = 255)
    var originalFilename: String = "",

    /** 实际保存时的文件名。 */
    @Column(name = "stored_filename", nullable = false, length = 255)
    var storedFilename: String = "",

    /** 文件扩展名，例如 jpg、png、webp。 */
    @Column(name = "extension", length = 32)
    var extension: String? = null,

    /** 文件 MIME 类型，例如 image/jpeg。 */
    @Column(name = "content_type", nullable = false, length = 128)
    var contentType: String = "",

    /** 文件大小，单位字节。 */
    @Column(name = "file_size", nullable = false)
    var fileSize: Long = 0,

    /** 图片宽度，单位像素。 */
    @Column(name = "width")
    var width: Int? = null,

    /** 图片高度，单位像素。 */
    @Column(name = "height")
    var height: Int? = null,

    /** 文件 SHA-256 摘要，用于校验和去重判断。 */
    @Column(name = "sha256", length = 64)
    var sha256: String? = null,

    /** 存储服务名称或存储桶名称。 */
    @Column(name = "bucket", length = 128)
    var bucket: String? = null,

    /** 对象存储中的对象键，或本地存储的相对路径。 */
    @Column(name = "object_key", nullable = false, length = 512)
    var objectKey: String = "",

    /** 服务端文件存储路径。 */
    @Column(name = "storage_path", nullable = false, length = 1024)
    var storagePath: String = "",

    /** 对外访问地址。 */
    @Column(name = "access_url", nullable = false, length = 1024)
    var accessUrl: String = "",

    /** 上传人标识。 */
    @Column(name = "uploaded_by", length = 128)
    var uploadedBy: String? = null,

    /** 图片用途或业务分组，例如 banner、inspection、overview。 */
    @Column(name = "usage_type", length = 64)
    var usageType: String? = null,

    /** 是否已删除。 */
    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    /** 创建时间。 */
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null,

    /** 更新时间。 */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = LocalDateTime.now()
        createdAt = createdAt ?: now
        updatedAt = updatedAt ?: now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
