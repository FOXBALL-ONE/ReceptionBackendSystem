package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 图片文件元数据仓库。
 */
interface ImageRepository : ReceptionRepository<Image, Long> {
    fun findByIsDeletedFalseOrderByCreatedAtDesc(): List<Image>

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
