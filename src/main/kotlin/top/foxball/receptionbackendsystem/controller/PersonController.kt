package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityUnitRequest
import top.foxball.receptionbackendsystem.controller.request.CreatePersonRequest
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.controller.request.UpdatePersonRequest
import top.foxball.receptionbackendsystem.service.PersonService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 活动人员接口。
 */
@RestController
@RequestMapping("/api/persons")
class PersonController(
    private val personService: PersonService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部人员。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(personService.findAll())

    /** 根据主键查询人员。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(personService.findById(request.id))

    /** 查询指定活动下的全部人员。 */
    @PostMapping("/find-by-activity")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> =
        ok(personService.findByActivityId(request.activityId))

    /** 根据姓名模糊查询人员。 */
    @PostMapping("/search")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<Response> =
        ok(personService.findByNameContaining(request.name))

    /** 查询指定活动下指定单位的人员。 */
    @PostMapping("/find-by-activity-and-unit")
    fun findByActivityIdAndUnit(@RequestBody request: ActivityUnitRequest): ResponseEntity<Response> =
        ok(personService.findByActivityIdAndUnit(request.activityId, request.unit))

    /** 在指定活动下创建人员。 */
    @PostMapping("/create")
    fun create(@RequestBody request: CreatePersonRequest): ResponseEntity<Response> =
        ok(personService.create(request.activityId, request.person), "人员创建成功")

    /** 更新人员信息。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdatePersonRequest): ResponseEntity<Response> =
        ok(personService.update(request.id, request.person), "人员更新成功")

    /** 删除人员。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        personService.delete(request.id)
        return ok(mapOf("id" to request.id), "人员删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
