package top.foxball.receptionbackendsystem.service

import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import java.nio.file.Path

/**
 * 图片业务服务契约。
 *
 * 为 [Image] 提供按活动维度的 CRUD、软删除、文件上传、SHA-256 去重，
 * 以及文件物理路径解析与对外访问地址构造。
 */
interface ImageService : ActivityEntityService<Image, Long> {
    /** 返回全部未软删的图片。 */
    fun findAllActive(): List<Image>

    /** 按主键查询单张未软删的图片，不存在或已删除时返回 null。 */
    fun findActiveById(id: Long): Image?

    /** 返回指定活动下未软删的图片列表。 */
    fun findByActivityIdAndIsDeletedFalse(activityId: Long): List<Image>

    /** 按 SHA-256 查询未软删的图片，用于上传去重判定。 */
    fun findBySha256AndIsDeletedFalse(sha256: String): Image?

    /**
     * 上传图片并落库元数据。
     *
     * [activityId] 指定所属活动；[file] 为上传的文件；
     * [usageType] 标识用途（如 banner、inspection）；[uploadedBy] 为上传人标识。
     */
    fun uploadImage(
        activityId: Long,
        file: MultipartFile,
        usageType: String?,
        uploadedBy: String?,
    ): Image

    /** 按主键软删除图片，成功返回 true。 */
    fun softDeleteById(id: Long): Boolean

    /** 解析图片元数据对应的物理文件路径。 */
    fun resolveFile(image: Image): Path

    /** 构造图片的对外访问地址。 */
    fun buildAccessUrl(image: Image): String
}
