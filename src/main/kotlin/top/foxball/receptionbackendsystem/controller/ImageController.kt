package top.foxball.receptionbackendsystem.controller

import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.handlder.ParamErrorException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ImageService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/list")
    fun list(@RequestBody(required = false) request: ActivityIdRequest?): ResponseEntity<Response> {
        val images = request?.activityId
            ?.let { imageService.findByActivityIdAndIsDeletedFalse(it) }
            ?: imageService.findAllActive()
        return ok(images.map(::toResponse))
    }

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> {
        val id = request.id ?: return badRequest("id is required")
        val image = imageService.findActiveById(id) ?: return notFound("image not found")
        return ok(toResponse(image))
    }

    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun upload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("activityId", required = false) activityId: Long?,
        @RequestPart("metadata", required = false) metadata: ImageUploadMetadata?,
    ): ResponseEntity<Response> {
        val resolvedActivityId = activityId ?: metadata?.activityId
            ?: throw ParamErrorException("activityId is required")
        val image = imageService.uploadImage(
            activityId = resolvedActivityId,
            file = file,
            usageType = metadata?.usageType,
            uploadedBy = metadata?.uploadedBy,
        )
        return ok(toResponse(image))
    }

    @PostMapping("/download")
    fun download(@RequestBody request: LongIdRequest): ResponseEntity<InputStreamResource> {
        val id = request.id ?: throw ParamErrorException("id is required")
        val image = imageService.findActiveById(id) ?: throw ResourceNotFoundException("image not found")
        val file = imageService.resolveFile(image)
        if (!Files.isRegularFile(file)) {
            throw ResourceNotFoundException("image file not found")
        }

        val mediaType = runCatching { MediaType.parseMediaType(image.contentType) }
            .getOrDefault(MediaType.APPLICATION_OCTET_STREAM)
        val disposition = ContentDisposition.attachment()
            .filename(image.originalFilename, StandardCharsets.UTF_8)
            .build()
        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(Files.size(file))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(InputStreamResource(Files.newInputStream(file)))
    }

    @PostMapping("/delete")
    fun delete(@RequestBody request: LongIdRequest): ResponseEntity<Response> {
        val id = request.id ?: return badRequest("id is required")
        if (!imageService.softDeleteById(id)) {
            return notFound("image not found")
        }
        return ok(true)
    }

    private fun toResponse(image: Image): ImageResponse =
        ImageResponse(
            id = image.id,
            activityId = image.activity?.id,
            originalFilename = image.originalFilename,
            storedFilename = image.storedFilename,
            extension = image.extension,
            contentType = image.contentType,
            fileSize = image.fileSize,
            width = image.width,
            height = image.height,
            sha256 = image.sha256,
            bucket = image.bucket,
            objectKey = image.objectKey,
            storagePath = image.storagePath,
            accessUrl = imageService.buildAccessUrl(image),
            uploadedBy = image.uploadedBy,
            usageType = image.usageType,
            isDeleted = image.isDeleted,
            createdAt = image.createdAt,
            updatedAt = image.updatedAt,
        )
}

data class ImageUploadMetadata(
    val activityId: Long? = null,
    val usageType: String? = null,
    val uploadedBy: String? = null,
)

data class ImageResponse(
    val id: Long?,
    val activityId: Long?,
    val originalFilename: String,
    val storedFilename: String,
    val extension: String?,
    val contentType: String,
    val fileSize: Long,
    val width: Int?,
    val height: Int?,
    val sha256: String?,
    val bucket: String?,
    val objectKey: String,
    val storagePath: String,
    val accessUrl: String,
    val uploadedBy: String?,
    val usageType: String?,
    val isDeleted: Boolean,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?,
)
