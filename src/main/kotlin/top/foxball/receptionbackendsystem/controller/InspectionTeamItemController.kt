package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(inspectionTeamItemService.findAll())

    /** 根据主键查询考察组安排。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> = ok(inspectionTeamItemService.findById(id))

    /** 根据考察组名称模糊查询安排。 */
    @GetMapping("/search")
    fun findByNameContaining(@RequestParam name: String): ResponseEntity<Response> =
        ok(inspectionTeamItemService.findByNameContaining(name))

    /** 创建考察组安排。 */
    @PostMapping
    fun create(@RequestBody inspectionTeamItem: InspectionTeamItem): ResponseEntity<Response> =
        ok(inspectionTeamItemService.create(inspectionTeamItem), "考察组安排创建成功")

    /** 更新考察组安排。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody inspectionTeamItem: InspectionTeamItem,
    ): ResponseEntity<Response> = ok(inspectionTeamItemService.update(id, inspectionTeamItem), "考察组安排更新成功")

    /** 删除考察组安排。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        inspectionTeamItemService.delete(id)
        return ok(mapOf("id" to id), "考察组安排删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
