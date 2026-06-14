package top.foxball.receptionbackendsystem.controller

import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.config.ImageProperties
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.controller.request.ObjectKeyRequest
import top.foxball.receptionbackendsystem.controller.request.Sha256Request
import top.foxball.receptionbackendsystem.controller.request.UpdateImageRequest
import top.foxball.receptionbackendsystem.controller.request.UploadImageMetadataRequest
import top.foxball.receptionbackendsystem.controller.request.UsageTypeRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ImageService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * 图片文件元数据接口。
 */
@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService,
    private val imageProperties: ImageProperties,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部未删除图片元数据。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(imageService.findAll())

    /** 根据主键查询图片元数据。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        ok(imageService.findById(request.id))

    /** 根据对象键查询图片元数据。 */
    @PostMapping("/find-by-object-key")
    fun findByObjectKey(@RequestBody request: ObjectKeyRequest): ResponseEntity<Response> =
        ok(imageService.findByObjectKey(request.objectKey))

    /** 根据 SHA-256 摘要查询图片元数据。 */
    @PostMapping("/find-by-sha256")
    fun findBySha256(@RequestBody request: Sha256Request): ResponseEntity<Response> =
        ok(imageService.findBySha256(request.sha256))

    /** 根据用途类型查询图片元数据。 */
    @PostMapping("/find-by-usage-type")
    fun findByUsageType(@RequestBody request: UsageTypeRequest): ResponseEntity<Response> =
        ok(imageService.findByUsageType(request.usageType))

    /** 创建图片元数据。 */
    @PostMapping("/create")
    fun create(@RequestBody image: Image): ResponseEntity<Response> =
        ok(imageService.create(image), "图片元数据创建成功")

    /** 更新图片元数据。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateImageRequest): ResponseEntity<Response> =
        ok(imageService.update(request.id, request.image), "图片元数据更新成功")

    /** 上传图片文件，并保存相对路径元数据。 */
    @PostMapping("/upload")
    fun upload(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("metadata", required = false) metadata: UploadImageMetadataRequest?,
        @RequestPart("usageType", required = false) usageType: String?,
        @RequestPart("uploadedBy", required = false) uploadedBy: String?,
    ): ResponseEntity<Response> =
        ok(
            imageService.upload(
                file = file,
                usageType = metadata?.usageType ?: usageType,
                uploadedBy = metadata?.uploadedBy ?: uploadedBy,
            ),
            "图片上传成功",
        )

    @PostMapping("/download")
    fun download(@RequestBody request: LongIdRequest): ResponseEntity<Resource> {
        val image = imageService.findById(request.id)
        val path = imageProperties.resolveStoragePath(image.storagePath)
        if (!Files.isRegularFile(path)) {
            throw ResourceNotFoundException("图片文件不存在：${image.storagePath}")
        }

        val filename = image.originalFilename.ifBlank { image.storedFilename }
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        val mediaType = runCatching { MediaType.parseMediaType(image.contentType) }
            .getOrDefault(MediaType.APPLICATION_OCTET_STREAM)

        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(Files.size(path))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFilename")
            .body(FileSystemResource(path))
    }

    /** 软删除图片元数据。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: LongIdRequest): ResponseEntity<Response> {
        imageService.delete(request.id)
        return ok(mapOf("id" to request.id), "图片元数据删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
