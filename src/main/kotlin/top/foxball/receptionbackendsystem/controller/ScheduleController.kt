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
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
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
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.saveOne(entity)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = Response(
            id = schedule.id,
            activityId = schedule.activity?.id,
            scheduleTags = schedule.scheduleTags,
            inspectionTeamItem = schedule.inspectionTeamItem,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.saveBatch(request.items)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = schedules.map { schedule ->
            Response(
                id = schedule.id,
                activityId = schedule.activity?.id,
                scheduleTags = schedule.scheduleTags,
                inspectionTeamItem = schedule.inspectionTeamItem,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.updateOne(entity)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = Response(
            id = schedule.id,
            activityId = schedule.activity?.id,
            scheduleTags = schedule.scheduleTags,
            inspectionTeamItem = schedule.inspectionTeamItem,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.updateBatch(request.items)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = schedules.map { schedule ->
            Response(
                id = schedule.id,
                activityId = schedule.activity?.id,
                scheduleTags = schedule.scheduleTags,
                inspectionTeamItem = schedule.inspectionTeamItem,
            )
        }

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

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = Response(
            id = schedule.id,
            activityId = schedule.activity?.id,
            scheduleTags = schedule.scheduleTags,
            inspectionTeamItem = schedule.inspectionTeamItem,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val schedules = scheduleService.findByActivityId(activityId)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = schedules.map { schedule ->
            Response(
                id = schedule.id,
                activityId = schedule.activity?.id,
                scheduleTags = schedule.scheduleTags,
                inspectionTeamItem = schedule.inspectionTeamItem,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-schedule-tags-containing")
    fun findByScheduleTagsContaining(@RequestBody request: ScheduleTagsRequest): ResponseEntity<ApiResponse> {
        val scheduleTags = request.scheduleTags?.takeIf { it.isNotBlank() }
            ?: return badRequest("scheduleTags is required")
        val schedules = scheduleService.findByScheduleTagsContaining(scheduleTags)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val scheduleTags: String?,
            val inspectionTeamItem: List<InspectionTeamItem>,
        )

        val rs = schedules.map { schedule ->
            Response(
                id = schedule.id,
                activityId = schedule.activity?.id,
                scheduleTags = schedule.scheduleTags,
                inspectionTeamItem = schedule.inspectionTeamItem,
            )
        }

        return builder.ok().data(rs).build()
    }
}
