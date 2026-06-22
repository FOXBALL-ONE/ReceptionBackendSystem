package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdsRequest
import top.foxball.receptionbackendsystem.controller.request.ScheduleTagsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.service.ScheduleService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Schedule): ResponseEntity<Response> = ok(scheduleService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<Response> =
        ok(scheduleService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Schedule): ResponseEntity<Response> = ok(scheduleService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<Response> =
        ok(scheduleService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        deleteById(request.id, scheduleService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, scheduleService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        findById(request.id, scheduleService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(scheduleService.findByActivityId(activityId))
    }

    @PostMapping("/find-by-schedule-tags-containing")
    fun findByScheduleTagsContaining(@RequestBody request: ScheduleTagsRequest): ResponseEntity<Response> {
        val scheduleTags = request.scheduleTags?.takeIf { it.isNotBlank() }
            ?: return badRequest("scheduleTags is required")
        return ok(scheduleService.findByScheduleTagsContaining(scheduleTags))
    }
}
