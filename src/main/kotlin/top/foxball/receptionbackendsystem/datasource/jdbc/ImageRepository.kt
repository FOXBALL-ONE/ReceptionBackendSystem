package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository

/**
 * 图片文件元数据数据仓库。
 */
interface ImageRepository : JpaRepository<Image, Long> {
    /**
     * 查询所有未删除图片，并按创建时间倒序排序。
     */
    fun findByIsDeletedFalseOrderByCreatedAtDesc(): List<Image>

    /**
     * 根据对象键查询未删除图片。
     */
    fun findByObjectKeyAndIsDeletedFalse(objectKey: String): Image?

    /**
     * 根据 SHA-256 摘要查询未删除图片。
     */
    fun findBySha256AndIsDeletedFalse(sha256: String): Image?

    /**
     * 根据用途类型查询未删除图片，并按创建时间倒序排序。
     */
    fun findByUsageTypeAndIsDeletedFalseOrderByCreatedAtDesc(usageType: String): List<Image>
}
