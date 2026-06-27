package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.config.ImageStorageProperties
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.ImageRepository
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ImageService
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.HexFormat
import javax.imageio.ImageIO

/**
 * 图片服务实现，操作 [Image] 实体。
 *
 * 负责图片文件落盘与元数据入库：以活动为单位建立存储目录，按 SHA-256 去重命名，
 * 并校验路径必须位于配置的存储根目录之内，防止目录穿越。
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 */
@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val activitiesRepository: ActivitiesRepository,
    private val imageStorageProperties: ImageStorageProperties,
) : AbstractReceptionService<Image, Long>(imageRepository), ImageService {
    private val extensionPattern = Regex("[A-Za-z0-9]{1,16}")

    /** 查询全部未软删的图片，按创建时间倒序。 */
    @Transactional(readOnly = true)
    override fun findAllActive(): List<Image> =
        imageRepository.findByIsDeletedFalseOrderByCreatedAtDesc()

    /** 按主键查询未软删的图片。 */
    @Transactional(readOnly = true)
    override fun findActiveById(id: Long): Image? =
        imageRepository.findByIdAndIsDeletedFalse(id)

    /** 按活动查询其下全部图片（含软删）。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Image> =
        imageRepository.findByActivityId(activityId)

    /** 按活动查询其下未软删的图片。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndIsDeletedFalse(activityId: Long): List<Image> =
        imageRepository.findByActivityIdAndIsDeletedFalse(activityId)

    /** 按 SHA-256 查询未软删的图片，用于上传去重判断。 */
    @Transactional(readOnly = true)
    override fun findBySha256AndIsDeletedFalse(sha256: String): Image? =
        imageRepository.findBySha256AndIsDeletedFalse(sha256)

    /**
     * 上传图片：落盘并入库元数据。
     *
     * 步骤：1) 校验文件非空、活动存在；
     * 2) 在存储根下按活动 id 建目录，并校验路径未越界；
     * 3) 写入临时文件并同步计算 SHA-256、读取图片尺寸，借此判断是否为合法图片；
     * 4) 解析扩展名（优先文件名后缀，回退 Content-Type 映射），以 SHA-256 为名原子移动到目标路径；
     * 5) 落库图片元数据（路径、尺寸、上传人、用途等）；异常时清理临时文件。
     */
    @Transactional
    override fun uploadImage(
        activityId: Long,
        file: MultipartFile,
        usageType: String?,
        uploadedBy: String?,
    ): Image {
        if (file.isEmpty) {
            throw ParamErrorException("file is required")
        }

        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val originalFilename = normalizeFilename(file.originalFilename)
        val contentType = file.contentType?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        val activityDirectory = imageStorageProperties.storageRoot().resolve(activityId.toString()).normalize()
        ensureInsideStorageRoot(activityDirectory)
        Files.createDirectories(activityDirectory)

        val tempFile = Files.createTempFile(activityDirectory, ".upload-", ".tmp")
        try {
            val sha256 = copyAndHash(file, tempFile)
            val dimensions = readDimensions(tempFile)
            if (!contentType.lowercase().startsWith("image/") && dimensions == null) {
                throw ParamErrorException("file must be an image")
            }

            val extension = resolveExtension(originalFilename, contentType)
            val storedFilename = if (extension == null) sha256 else "$sha256.$extension"
            val targetFile = activityDirectory.resolve(storedFilename).normalize()
            ensureInsideStorageRoot(targetFile)
            moveReplacing(tempFile, targetFile)

            val relativePath = "/$activityId/$storedFilename"
            return imageRepository.saveOne(
                Image(
                    activity = activity,
                    originalFilename = originalFilename,
                    storedFilename = storedFilename,
                    extension = extension,
                    contentType = contentType,
                    fileSize = Files.size(targetFile),
                    width = dimensions?.first,
                    height = dimensions?.second,
                    sha256 = sha256,
                    objectKey = relativePath,
                    storagePath = relativePath,
                    accessUrl = relativePath,
                    uploadedBy = uploadedBy?.takeIf { it.isNotBlank() },
                    usageType = usageType?.takeIf { it.isNotBlank() },
                )
            )
        } catch (ex: Exception) {
            Files.deleteIfExists(tempFile)
            throw ex
        }
    }

    /** 按 id 软删图片（置 [Image.isDeleted] 为 true），未找到返回 false。 */
    @Transactional
    override fun softDeleteById(id: Long): Boolean {
        val image = imageRepository.findByIdAndIsDeletedFalse(id) ?: return false
        image.isDeleted = true
        imageRepository.saveOne(image)
        return true
    }

    /** 解析图片在磁盘上的绝对路径，并校验路径仍在存储根目录之内。 */
    override fun resolveFile(image: Image): Path {
        val relativePath = image.objectKey.ifBlank { image.storagePath }
        val file = imageStorageProperties.storageRoot()
            .resolve(relativePath.trimStart('/', '\\'))
            .normalize()
        ensureInsideStorageRoot(file)
        return file
    }

    /** 基于配置的 baseUrl 与图片相对路径拼装对外可访问 URL。 */
    override fun buildAccessUrl(image: Image): String {
        val relativePath = image.objectKey.ifBlank { image.storagePath }
        val baseUrl = imageStorageProperties.baseUrl.trimEnd('/')
        val path = relativePath.trimStart('/', '\\')
        return if (baseUrl.isBlank()) "/$path" else "$baseUrl/$path"
    }

    private fun copyAndHash(file: MultipartFile, target: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream.use { input ->
            Files.newOutputStream(target).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    digest.update(buffer, 0, read)
                    output.write(buffer, 0, read)
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest())
    }

    private fun readDimensions(file: Path): Pair<Int, Int>? =
        ImageIO.read(file.toFile())?.let { image -> image.width to image.height }

    private fun normalizeFilename(filename: String?): String =
        filename
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
            ?: "image"

    private fun resolveExtension(filename: String, contentType: String): String? {
        val fromName = filename.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
            .takeIf { it.matches(extensionPattern) }
        if (fromName != null) {
            return fromName
        }
        return when (contentType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            "image/svg+xml" -> "svg"
            "image/bmp" -> "bmp"
            else -> null
        }
    }

    private fun moveReplacing(source: Path, target: Path) {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun ensureInsideStorageRoot(path: Path) {
        val root = imageStorageProperties.storageRoot()
        if (!path.toAbsolutePath().normalize().startsWith(root)) {
            throw ParamErrorException("invalid storage path")
        }
    }
}
