package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.UpdateActivityRequest
import top.foxball.receptionbackendsystem.controller.request.UrlRequest
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
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(activitiesService.findAll())

    /** 查询所有已开放活动。 */
    @PostMapping("/open")
    fun findOpenActivities(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(activitiesService.findOpenActivities())

    /** 根据主键查询活动。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(activitiesService.findById(request.id))

    /** 根据访问地址查询活动。 */
    @PostMapping("/find-by-url")
    fun findByUrl(@RequestBody request: UrlRequest): ResponseEntity<Response> =
        ok(activitiesService.findByUrl(request.url))

    /** 创建活动。 */
    @PostMapping("/create")
    fun create(@RequestBody activity: Activities): ResponseEntity<Response> =
        ok(activitiesService.create(activity), "活动创建成功")

    /** 更新活动。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateActivityRequest): ResponseEntity<Response> =
        ok(activitiesService.update(request.id, request.activity), "活动更新成功")

    /** 删除活动。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        activitiesService.delete(request.id)
        return ok(mapOf("id" to request.id), "活动删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
