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

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val activitiesRepository: ActivitiesRepository,
    private val imageStorageProperties: ImageStorageProperties,
) : AbstractReceptionService<Image, Long>(imageRepository), ImageService {
    private val extensionPattern = Regex("[A-Za-z0-9]{1,16}")

    @Transactional(readOnly = true)
    override fun findAllActive(): List<Image> =
        imageRepository.findByIsDeletedFalseOrderByCreatedAtDesc()

    @Transactional(readOnly = true)
    override fun findActiveById(id: Long): Image? =
        imageRepository.findByIdAndIsDeletedFalse(id)

    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Image> =
        imageRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndIsDeletedFalse(activityId: Long): List<Image> =
        imageRepository.findByActivityIdAndIsDeletedFalse(activityId)

    @Transactional(readOnly = true)
    override fun findBySha256AndIsDeletedFalse(sha256: String): Image? =
        imageRepository.findBySha256AndIsDeletedFalse(sha256)

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

    @Transactional
    override fun softDeleteById(id: Long): Boolean {
        val image = imageRepository.findByIdAndIsDeletedFalse(id) ?: return false
        image.isDeleted = true
        imageRepository.saveOne(image)
        return true
    }

    override fun resolveFile(image: Image): Path {
        val relativePath = image.objectKey.ifBlank { image.storagePath }
        val file = imageStorageProperties.storageRoot()
            .resolve(relativePath.trimStart('/', '\\'))
            .normalize()
        ensureInsideStorageRoot(file)
        return file
    }

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
