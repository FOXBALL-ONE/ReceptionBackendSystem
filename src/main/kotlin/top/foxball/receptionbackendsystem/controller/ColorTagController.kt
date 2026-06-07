package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.service.ColorTagService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 颜色标签接口。
 */
@RestController
@RequestMapping("/api/color-tags")
class ColorTagController(
    private val colorTagService: ColorTagService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部颜色标签。 */
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(colorTagService.findAll())

    /** 根据主键查询颜色标签。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<Response> = ok(colorTagService.findById(id))

    /** 根据名称查询颜色标签。 */
    @GetMapping("/by-name")
    fun findByName(@RequestParam name: String): ResponseEntity<Response> = ok(colorTagService.findByName(name))

    /** 创建颜色标签。 */
    @PostMapping
    fun create(@RequestBody colorTag: ColorTag): ResponseEntity<Response> =
        ok(colorTagService.create(colorTag), "颜色标签创建成功")

    /** 更新颜色标签。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody colorTag: ColorTag,
    ): ResponseEntity<Response> = ok(colorTagService.update(id, colorTag), "颜色标签更新成功")

    /** 删除颜色标签。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Response> {
        colorTagService.delete(id)
        return ok(mapOf("id" to id), "颜色标签删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
