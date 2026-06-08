package top.foxball.receptionbackendsystem.service.impl

import org.springframework.util.StringUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.config.ImageProperties
import top.foxball.receptionbackendsystem.controller.response.ImageResponse
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.ImageRepository
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ImageService
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import javax.imageio.ImageIO

/**
 * 图片文件元数据业务服务实现。
 */
@Service
@Transactional
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val imageProperties: ImageProperties,
) : ImageService {

    /** 查询全部未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<ImageResponse> =
        imageRepository.findByIsDeletedFalseOrderByCreatedAtDesc().map(::toResponse)

    /** 根据主键查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findById(id: Long): ImageResponse = toResponse(findEntityById(id))

    /** 根据对象键查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findByObjectKey(objectKey: String): ImageResponse {
        val relativePath = imageProperties.normalizeRelativePath(objectKey)
        return toResponse(
            imageRepository.findByObjectKeyAndIsDeletedFalse(relativePath)
                ?: throw ResourceNotFoundException("图片不存在：$objectKey")
        )
    }

    /** 根据 SHA-256 摘要查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findBySha256(sha256: String): ImageResponse =
        toResponse(
            imageRepository.findBySha256AndIsDeletedFalse(sha256)
                ?: throw ResourceNotFoundException("图片不存在：$sha256")
        )

    /** 根据用途类型查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findByUsageType(usageType: String): List<ImageResponse> =
        imageRepository.findByUsageTypeAndIsDeletedFalseOrderByCreatedAtDesc(usageType).map(::toResponse)

    /** 创建图片元数据。 */
    override fun create(image: Image): ImageResponse {
        image.id = null
        image.isDeleted = false
        normalizeImagePaths(image)
        return toResponse(imageRepository.save(image))
    }

    /** 更新图片元数据，保留创建时间和删除状态。 */
    override fun update(id: Long, image: Image): ImageResponse {
        val existing = findEntityById(id)

        image.id = id
        image.createdAt = existing.createdAt
        image.isDeleted = existing.isDeleted
        normalizeImagePaths(image)
        return toResponse(imageRepository.save(image))
    }

    /** 上传图片文件并创建元数据。 */
    override fun upload(file: MultipartFile, usageType: String?, uploadedBy: String?): ImageResponse {
        if (file.isEmpty) {
            throw ParamErrorException("上传图片不能为空")
        }

        val originalFilename = StringUtils.cleanPath(file.originalFilename?.takeIf { it.isNotBlank() } ?: "image")
        val extension = originalFilename.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .takeIf { it.isNotBlank() }
        val storedFilename = "${UUID.randomUUID()}${extension?.let { ".$it" } ?: ""}"
        val relativePath = "${sanitizePathSegment(usageType)}/$storedFilename"
        val targetPath = imageProperties.resolveStoragePath(relativePath)

        Files.createDirectories(targetPath.parent)
        file.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val imageSize = runCatching { ImageIO.read(targetPath.toFile()) }.getOrNull()
        val normalizedRelativePath = imageProperties.normalizeRelativePath(relativePath)
        val image = Image(
            originalFilename = originalFilename,
            storedFilename = storedFilename,
            extension = extension,
            contentType = file.contentType ?: "application/octet-stream",
            fileSize = Files.size(targetPath),
            width = imageSize?.width,
            height = imageSize?.height,
            sha256 = sha256(targetPath),
            objectKey = normalizedRelativePath,
            storagePath = normalizedRelativePath,
            accessUrl = normalizedRelativePath,
            uploadedBy = uploadedBy,
            usageType = usageType,
        )

        return toResponse(imageRepository.save(image))
    }

    /** 软删除图片元数据。 */
    override fun delete(id: Long) {
        val image = findEntityById(id)
        image.isDeleted = true
        imageRepository.save(image)
    }

    /** 查询未删除图片实体；仅供服务内部更新和删除时使用。 */
    private fun findEntityById(id: Long): Image = imageRepository.findById(id)
        .filter { !it.isDeleted }
        .orElseThrow { ResourceNotFoundException("图片不存在：$id") }

    /**
     * 统一规范化图片路径字段。
     *
     * 数据库只保存相对路径，因此 `storagePath`、`objectKey` 和 `accessUrl`
     * 在入库前都会被同步成同一个相对路径；真正的公开 URL 只在响应 DTO 中生成。
     */
    private fun normalizeImagePaths(image: Image) {
        val relativePath = imageProperties.normalizeRelativePath(
            image.storagePath.ifBlank { image.objectKey.ifBlank { image.accessUrl } }
        )
        image.storagePath = relativePath
        image.objectKey = relativePath
        image.accessUrl = relativePath

        if (image.storedFilename.isBlank()) {
            image.storedFilename = relativePath.substringAfterLast('/')
        }
        if (image.extension.isNullOrBlank()) {
            image.extension = image.storedFilename.substringAfterLast('.', missingDelimiterValue = "")
                .lowercase()
                .takeIf { it.isNotBlank() }
        }
    }

    /** 将数据库实体转换为前端响应对象。 */
    private fun toResponse(image: Image): ImageResponse = ImageResponse.from(image, imageProperties)

    /**
     * 将用途类型转换为安全目录名。
     *
     * 目录名只保留字母、数字、下划线和中划线，避免用户输入影响文件系统路径结构。
     */
    private fun sanitizePathSegment(value: String?): String {
        val sanitized = value
            ?.trim()
            ?.replace(Regex("[^A-Za-z0-9_-]"), "_")
            ?.trim('_')
        return sanitized?.takeIf { it.isNotBlank() } ?: "images"
    }

    /** 流式计算文件 SHA-256 摘要，避免一次性把大文件读入内存。 */
    private fun sha256(path: java.nio.file.Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
