package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
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
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(personService.findAll())

    /** 根据主键查询人员。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<Response> = ok(personService.findById(id))

    /** 查询指定活动下的全部人员。 */
    @GetMapping("/activity/{activityId}")
    fun findByActivityId(@PathVariable activityId: Int): ResponseEntity<Response> =
        ok(personService.findByActivityId(activityId))

    /** 根据姓名模糊查询人员。 */
    @GetMapping("/search")
    fun findByNameContaining(@RequestParam name: String): ResponseEntity<Response> =
        ok(personService.findByNameContaining(name))

    /** 查询指定活动下指定单位的人员。 */
    @GetMapping("/activity/{activityId}/unit")
    fun findByActivityIdAndUnit(
        @PathVariable activityId: Int,
        @RequestParam unit: String,
    ): ResponseEntity<Response> = ok(personService.findByActivityIdAndUnit(activityId, unit))

    /** 在指定活动下创建人员。 */
    @PostMapping("/activity/{activityId}")
    fun create(
        @PathVariable activityId: Int,
        @RequestBody person: Person,
    ): ResponseEntity<Response> = ok(personService.create(activityId, person), "人员创建成功")

    /** 更新人员信息。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody person: Person,
    ): ResponseEntity<Response> = ok(personService.update(id, person), "人员更新成功")

    /** 删除人员。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Response> {
        personService.delete(id)
        return ok(mapOf("id" to id), "人员删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
