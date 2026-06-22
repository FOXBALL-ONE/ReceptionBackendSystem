package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.BatchSaveSchedulesRequest
import top.foxball.receptionbackendsystem.controller.request.CreateScheduleRequest
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.controller.request.ScheduleTagsRequest
import top.foxball.receptionbackendsystem.controller.request.UpdateScheduleRequest
import top.foxball.receptionbackendsystem.service.ScheduleService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 活动日程接口。
 */
@RestController
@RequestMapping("/api/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部日程。 */
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(scheduleService.findAll())

    /** 根据主键查询日程。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        ok(scheduleService.findById(request.id))

    /** 查询指定活动下的全部日程。 */
    @PostMapping("/find-by-activity")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> =
        ok(scheduleService.findByActivityId(request.activityId))

    /** 根据日程标签模糊查询。 */
    @PostMapping("/search")
    fun findByScheduleTagsContaining(@RequestBody request: ScheduleTagsRequest): ResponseEntity<Response> =
        ok(scheduleService.findByScheduleTagsContaining(request.scheduleTags))

    /** 在指定活动下创建日程。 */
    @PostMapping("/create")
    fun create(@RequestBody request: CreateScheduleRequest): ResponseEntity<Response> =
        ok(scheduleService.create(request.activityId, request.schedule), "日程创建成功")

    /** 更新日程信息。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateScheduleRequest): ResponseEntity<Response> =
        ok(scheduleService.update(request.id, request.schedule), "日程更新成功")

    /** 删除日程。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: LongIdRequest): ResponseEntity<Response> {
        scheduleService.delete(request.id)
        return ok(mapOf("id" to request.id), "日程删除成功")
    }

    /** 批量保存日程。 */
    @PostMapping("/save")
    fun batchSave(@RequestBody request: BatchSaveSchedulesRequest): ResponseEntity<Response> =
        ok(scheduleService.batchSave(request.activityId, request.schedules), "保存成功")

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
