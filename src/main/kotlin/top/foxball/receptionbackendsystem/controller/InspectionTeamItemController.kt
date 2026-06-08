package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.controller.request.UpdateInspectionTeamItemRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.service.InspectionTeamItemService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 考察组安排接口。
 */
@RestController
@RequestMapping("/api/inspection-team-items")
class InspectionTeamItemController(
    private val inspectionTeamItemService: InspectionTeamItemService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部考察组安排。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(inspectionTeamItemService.findAll())

    /** 根据主键查询考察组安排。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        ok(inspectionTeamItemService.findById(request.id))

    /** 根据考察组名称模糊查询安排。 */
    @PostMapping("/search")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<Response> =
        ok(inspectionTeamItemService.findByNameContaining(request.name))

    /** 创建考察组安排。 */
    @PostMapping("/create")
    fun create(@RequestBody inspectionTeamItem: InspectionTeamItem): ResponseEntity<Response> =
        ok(inspectionTeamItemService.create(inspectionTeamItem), "考察组安排创建成功")

    /** 更新考察组安排。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateInspectionTeamItemRequest): ResponseEntity<Response> =
        ok(
            inspectionTeamItemService.update(request.id, request.inspectionTeamItem),
            "考察组安排更新成功"
        )

    /** 删除考察组安排。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: LongIdRequest): ResponseEntity<Response> {
        inspectionTeamItemService.delete(request.id)
        return ok(mapOf("id" to request.id), "考察组安排删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
