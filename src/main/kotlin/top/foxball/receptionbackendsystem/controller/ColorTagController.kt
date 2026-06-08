package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.controller.request.UpdateColorTagRequest
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
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(colorTagService.findAll())

    /** 根据主键查询颜色标签。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(colorTagService.findById(request.id))

    /** 根据名称查询颜色标签。 */
    @PostMapping("/find-by-name")
    fun findByName(@RequestBody request: NameRequest): ResponseEntity<Response> =
        ok(colorTagService.findByName(request.name))

    /** 创建颜色标签。 */
    @PostMapping("/create")
    fun create(@RequestBody colorTag: ColorTag): ResponseEntity<Response> =
        ok(colorTagService.create(colorTag), "颜色标签创建成功")

    /** 更新颜色标签。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateColorTagRequest): ResponseEntity<Response> =
        ok(colorTagService.update(request.id, request.colorTag), "颜色标签更新成功")

    /** 删除颜色标签。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        colorTagService.delete(request.id)
        return ok(mapOf("id" to request.id), "颜色标签删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
