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

/**
 * 日程控制器，挂在 /api/schedules 下。
 *
 * 提供按活动整体保存、单条/批量保存与更新、删除、按活动/标签查询等日程的增删改查端点。
 */
@RestController
@RequestMapping("/api/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 按活动整体保存日程，覆盖该活动下既有记录。 */
    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: ScheduleSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val schedules = scheduleService.saveByActivity(activityId, request.schedules)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 新增单条日程并返回。 */
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.saveOne(entity)

        return builder.ok().data(schedule.toResponse()).build()
    }

    /** 批量新增日程并返回。 */
    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.saveBatch(request.items)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 更新单条日程并返回。 */
    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Schedule): ResponseEntity<ApiResponse> {
        val schedule = scheduleService.updateOne(entity)

        return builder.ok().data(schedule.toResponse()).build()
    }

    /** 批量更新日程并返回。 */
    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Schedule>): ResponseEntity<ApiResponse> {
        val schedules = scheduleService.updateBatch(request.items)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 按 id 删除单条日程。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, scheduleService)

    /** 按 id 列表批量删除日程。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, scheduleService)

    /** 按 id 查询单条日程，未找到返回 notFound。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val schedule = scheduleService.findEntityById(id) ?: return notFound("entity not found")

        return builder.ok().data(schedule.toResponse()).build()
    }

    /** 按活动 id 查询该活动下所有日程。 */
    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val schedules = scheduleService.findByActivityId(activityId)

        val rs = schedules.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 按日程标签模糊查询日程。 */
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
