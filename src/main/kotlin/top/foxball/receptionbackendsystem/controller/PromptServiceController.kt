package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.UpdatePromptServiceRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.service.PromptServiceService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 提示服务配置接口。
 */
@RestController
@RequestMapping("/api/prompt-services")
class PromptServiceController(
    private val promptServiceService: PromptServiceService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部提示服务配置。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(promptServiceService.findAll())

    /** 根据主键查询提示服务配置。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(promptServiceService.findById(request.id))

    /** 创建提示服务配置。 */
    @PostMapping("/create")
    fun create(@RequestBody promptService: PromptService): ResponseEntity<Response> =
        ok(promptServiceService.create(promptService), "提示服务配置创建成功")

    /** 更新提示服务配置。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdatePromptServiceRequest): ResponseEntity<Response> =
        ok(promptServiceService.update(request.id, request.promptService), "提示服务配置更新成功")

    /** 删除提示服务配置。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        promptServiceService.delete(request.id)
        return ok(mapOf("id" to request.id), "提示服务配置删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
