package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.InspectionTeamSaveRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdRequest
import top.foxball.receptionbackendsystem.controller.request.LongIdsRequest
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.service.InspectionTeamService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 考察组控制器，挂在 /api/inspection-teams 下。
 *
 * 提供按活动整体保存、单条/批量保存与更新、删除、按活动/名称/ id 查询等考察组的增删改查端点。
 */
@RestController
@RequestMapping("/api/inspection-teams")
class InspectionTeamController(
    private val inspectionTeamService: InspectionTeamService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 按活动整体保存考察组，覆盖该活动下既有记录。 */
    @PostMapping("/save-by-activity")
    fun saveByActivity(@RequestBody request: InspectionTeamSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val groups = inspectionTeamService.saveByActivity(activityId, request.items)

        val rs = groups.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 新增单条考察组并返回。 */
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<ApiResponse> {
        val inspectionTeam = inspectionTeamService.saveOne(entity)

        return builder.ok().data(inspectionTeam.toResponse()).build()
    }

    /** 批量新增考察组并返回。 */
    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<ApiResponse> {
        val inspectionTeams = inspectionTeamService.saveBatch(request.items)

        val rs = inspectionTeams.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 更新单条考察组并返回。 */
    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<ApiResponse> {
        val inspectionTeam = inspectionTeamService.updateOne(entity)

        return builder.ok().data(inspectionTeam.toResponse()).build()
    }

    /** 批量更新考察组并返回。 */
    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<ApiResponse> {
        val inspectionTeams = inspectionTeamService.updateBatch(request.items)

        val rs = inspectionTeams.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 按 id 删除单条考察组。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, inspectionTeamService)

    /** 按 id 列表批量删除考察组。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, inspectionTeamService)

    /** 按 id 查询单条考察组，未找到返回 notFound。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val inspectionTeam = inspectionTeamService.findEntityById(id) ?: return notFound("entity not found")

        return builder.ok().data(inspectionTeam.toResponse()).build()
    }

    /** 按活动 id 查询该活动下所有考察组。 */
    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val inspectionTeams = inspectionTeamService.findByActivityId(activityId)

        val rs = inspectionTeams.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    /** 按名称模糊查询考察组。 */
    @PostMapping("/find-by-name-containing")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<ApiResponse> {
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val inspectionTeams = inspectionTeamService.findByNameContaining(name)

        val rs = inspectionTeams.map { it.toResponse() }

        return builder.ok().data(rs).build()
    }

    private fun InspectionTeamItem.toResponse() = InspectionTeamResponse(
        id = id,
        activityId = activity?.id,
        name = name,
        itineraries = itineraries.map { itinerary ->
            InspectionTeamItineraryResponse(
                id = itinerary.id,
                scheduleId = itinerary.schedule?.id,
                routeUrl = itinerary.routeUrl,
                scheduleUrl = itinerary.scheduleUrl,
                routeNode = itinerary.routeNode,
                eventArrangements = itinerary.eventArrangements,
            )
        },
    )

    private data class InspectionTeamResponse(
        val id: Long?,
        val activityId: Long?,
        val name: String?,
        val itineraries: List<InspectionTeamItineraryResponse>,
    )

    private data class InspectionTeamItineraryResponse(
        val id: Long?,
        val scheduleId: Long?,
        val routeUrl: String?,
        val scheduleUrl: String?,
        val routeNode: List<String>,
        val eventArrangements: List<EventArrangementsItem>,
    )
}
