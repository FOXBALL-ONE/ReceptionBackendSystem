package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.service.ActivitiesService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 活动配置接口。
 */
@RestController
@RequestMapping("/api/activities")
class ActivitiesController(
    private val activitiesService: ActivitiesService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部活动。 */
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(activitiesService.findAll())

    /** 查询所有已开放活动。 */
    @GetMapping("/open")
    fun findOpenActivities(): ResponseEntity<Response> = ok(activitiesService.findOpenActivities())

    /** 根据主键查询活动。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<Response> = ok(activitiesService.findById(id))

    /** 根据访问地址查询活动。 */
    @GetMapping("/by-url")
    fun findByUrl(@RequestParam url: String): ResponseEntity<Response> = ok(activitiesService.findByUrl(url))

    /** 创建活动。 */
    @PostMapping
    fun create(@RequestBody activity: Activities): ResponseEntity<Response> =
        ok(activitiesService.create(activity), "活动创建成功")

    /** 更新活动。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody activity: Activities,
    ): ResponseEntity<Response> = ok(activitiesService.update(id, activity), "活动更新成功")

    /** 删除活动。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Response> {
        activitiesService.delete(id)
        return ok(mapOf("id" to id), "活动删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
