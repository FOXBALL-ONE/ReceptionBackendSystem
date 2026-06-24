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
import top.foxball.receptionbackendsystem.controller.request.ScheduleSaveRequest
import top.foxball.receptionbackendsystem.controller.request.ScheduleTagsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.service.ScheduleService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: ScheduleSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val schedules = scheduleService.saveByActivity(activityId, request.schedules)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.saveOne(entity)

        return builder.ok().data(schedule.toResponse()).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.saveBatch(request.items)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.updateOne(entity)

        return builder.ok().data(schedule.toResponse()).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.updateBatch(request.items)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, scheduleService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, scheduleService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val schedule = scheduleService.findEntityById(id) ?: return notFound("entity not found")

        return builder.ok().data(schedule.toResponse()).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val schedules = scheduleService.findByActivityId(activityId)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-schedule-tags-containing")
    fun findByScheduleTagsContaining(@RequestBody request: ScheduleTagsRequest): ResponseEntity<ApiResponse> {
        val scheduleTags = request.scheduleTags?.takeIf { it.isNotBlank() }
            ?: return badRequest("scheduleTags is required")
        val schedules = scheduleService.findByScheduleTagsContaining(scheduleTags)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    private fun Schedule.toResponse() = ScheduleResponse(
        id = id,
        activityId = activity?.id,
        scheduleTags = scheduleTags,
    )

    private data class ScheduleResponse(
        val id: Long?,
        val activityId: Long?,
        val scheduleTags: String?,
    )
}
