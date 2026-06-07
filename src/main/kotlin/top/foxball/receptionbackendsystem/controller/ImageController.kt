package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.service.ImageService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 图片文件元数据接口。
 */
@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部未删除图片元数据。 */
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(imageService.findAll())

    /** 根据主键查询图片元数据。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> = ok(imageService.findById(id))

    /** 根据对象键查询图片元数据。 */
    @GetMapping("/by-object-key")
    fun findByObjectKey(@RequestParam objectKey: String): ResponseEntity<Response> =
        ok(imageService.findByObjectKey(objectKey))

    /** 根据 SHA-256 摘要查询图片元数据。 */
    @GetMapping("/by-sha256")
    fun findBySha256(@RequestParam sha256: String): ResponseEntity<Response> =
        ok(imageService.findBySha256(sha256))

    /** 根据用途类型查询图片元数据。 */
    @GetMapping("/usage/{usageType}")
    fun findByUsageType(@PathVariable usageType: String): ResponseEntity<Response> =
        ok(imageService.findByUsageType(usageType))

    /** 创建图片元数据。 */
    @PostMapping
    fun create(@RequestBody image: Image): ResponseEntity<Response> =
        ok(imageService.create(image), "图片元数据创建成功")

    /** 更新图片元数据。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody image: Image,
    ): ResponseEntity<Response> = ok(imageService.update(id, image), "图片元数据更新成功")

    /** 软删除图片元数据。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        imageService.delete(id)
        return ok(mapOf("id" to id), "图片元数据删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
