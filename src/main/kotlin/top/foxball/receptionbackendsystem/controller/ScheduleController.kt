package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
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
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(scheduleService.findAll())

    /** 根据主键查询日程。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> = ok(scheduleService.findById(id))

    /** 查询指定活动下的全部日程。 */
    @GetMapping("/activity/{activityId}")
    fun findByActivityId(@PathVariable activityId: Int): ResponseEntity<Response> =
        ok(scheduleService.findByActivityId(activityId))

    /** 根据日程标签模糊查询。 */
    @GetMapping("/search")
    fun findByScheduleTagsContaining(@RequestParam scheduleTags: String): ResponseEntity<Response> =
        ok(scheduleService.findByScheduleTagsContaining(scheduleTags))

    /** 在指定活动下创建日程。 */
    @PostMapping("/activity/{activityId}")
    fun create(
        @PathVariable activityId: Int,
        @RequestBody schedule: Schedule,
    ): ResponseEntity<Response> = ok(scheduleService.create(activityId, schedule), "日程创建成功")

    /** 更新日程信息。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody schedule: Schedule,
    ): ResponseEntity<Response> = ok(scheduleService.update(id, schedule), "日程更新成功")

    /** 删除日程。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        scheduleService.delete(id)
        return ok(mapOf("id" to id), "日程删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
