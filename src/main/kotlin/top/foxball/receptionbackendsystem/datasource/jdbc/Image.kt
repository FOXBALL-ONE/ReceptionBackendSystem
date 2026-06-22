package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * 图片文件元数据实体。
 *
 * 只保存图片文件的描述、存储定位和访问地址，不保存图片二进制内容。
 */
@Table(
    name = "images",
    indexes = [
        Index(name = "idx_images_activity_id", columnList = "activity_id"),
        Index(name = "idx_images_sha256", columnList = "sha256"),
        Index(name = "idx_images_object_key", columnList = "object_key"),
        Index(name = "idx_images_created_at", columnList = "created_at"),
    ],
)
@Entity
data class Image(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 上传时的原始文件名。 */
    @Column(name = "original_filename", nullable = false, length = 255)
    var originalFilename: String = "",

    /** 实际保存时的文件名。 */
    @Column(name = "stored_filename", nullable = false, length = 255)
    var storedFilename: String = "",

    /** 文件扩展名，例如 jpg、png、webp。 */
    @Column(name = "extension", length = 32)
    var extension: String? = null,

    /** 文件 MIME 类型。 */
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

    /** 文件 SHA-256 摘要。 */
    @Column(name = "sha256", length = 64)
    var sha256: String? = null,

    /** 存储桶或存储服务名称。 */
    @Column(name = "bucket", length = 128)
    var bucket: String? = null,

    /** 对象存储 key，或本地存储相对路径。 */
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
