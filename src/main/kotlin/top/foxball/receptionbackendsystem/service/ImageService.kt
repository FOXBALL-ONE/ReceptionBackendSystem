package top.foxball.receptionbackendsystem.service

import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import java.nio.file.Path

interface ImageService : ActivityEntityService<Image, Long> {
    fun findAllActive(): List<Image>

    fun findActiveById(id: Long): Image?

    fun findByActivityIdAndIsDeletedFalse(activityId: Long): List<Image>

    fun findBySha256AndIsDeletedFalse(sha256: String): Image?

    fun uploadImage(
        activityId: Long,
        file: MultipartFile,
        usageType: String?,
        uploadedBy: String?,
    ): Image

    fun softDeleteById(id: Long): Boolean

    fun resolveFile(image: Image): Path

    fun buildAccessUrl(image: Image): String
}
