package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 图片文件元数据仓库。
 */
interface ImageRepository : ReceptionRepository<Image, Long> {
    /**
     * 查询全部未删除的图片元数据，按创建时间倒序。
     */
    fun findByIsDeletedFalseOrderByCreatedAtDesc(): List<Image>

    /**
     * 根据 ID 查询未删除的图片元数据。
     */
    fun findByIdAndIsDeletedFalse(id: Long): Image?

    /**
     * 查询指定活动下的全部图片元数据。
     */
    fun findByActivityId(activityId: Long): List<Image>

    /**
     * 查询指定活动下未删除的图片元数据。
     */
    fun findByActivityIdAndIsDeletedFalse(activityId: Long): List<Image>

    /**
     * 根据 SHA-256 查询未删除的图片元数据。
     */
    fun findBySha256AndIsDeletedFalse(sha256: String): Image?
}
