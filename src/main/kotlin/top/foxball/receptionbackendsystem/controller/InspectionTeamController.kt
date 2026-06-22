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
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.service.InspectionTeamService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/inspection-teams")
class InspectionTeamController(
    private val inspectionTeamService: InspectionTeamService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<ApiResponse> {
        val inspectionTeam = inspectionTeamService.saveOne(entity)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = Response(
            id = inspectionTeam.id,
            activityId = inspectionTeam.activity?.id,
            name = inspectionTeam.name,
            routeUrl = inspectionTeam.routeUrl,
            scheduleUrl = inspectionTeam.scheduleUrl,
            routeNode = inspectionTeam.routeNode,
            eventArrangements = inspectionTeam.eventArrangements,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<ApiResponse> {
        val inspectionTeams = inspectionTeamService.saveBatch(request.items)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = inspectionTeams.map { inspectionTeam ->
            Response(
                id = inspectionTeam.id,
                activityId = inspectionTeam.activity?.id,
                name = inspectionTeam.name,
                routeUrl = inspectionTeam.routeUrl,
                scheduleUrl = inspectionTeam.scheduleUrl,
                routeNode = inspectionTeam.routeNode,
                eventArrangements = inspectionTeam.eventArrangements,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<ApiResponse> {
        val inspectionTeam = inspectionTeamService.updateOne(entity)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = Response(
            id = inspectionTeam.id,
            activityId = inspectionTeam.activity?.id,
            name = inspectionTeam.name,
            routeUrl = inspectionTeam.routeUrl,
            scheduleUrl = inspectionTeam.scheduleUrl,
            routeNode = inspectionTeam.routeNode,
            eventArrangements = inspectionTeam.eventArrangements,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<ApiResponse> {
        val inspectionTeams = inspectionTeamService.updateBatch(request.items)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = inspectionTeams.map { inspectionTeam ->
            Response(
                id = inspectionTeam.id,
                activityId = inspectionTeam.activity?.id,
                name = inspectionTeam.name,
                routeUrl = inspectionTeam.routeUrl,
                scheduleUrl = inspectionTeam.scheduleUrl,
                routeNode = inspectionTeam.routeNode,
                eventArrangements = inspectionTeam.eventArrangements,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, inspectionTeamService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, inspectionTeamService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val inspectionTeam = inspectionTeamService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = Response(
            id = inspectionTeam.id,
            activityId = inspectionTeam.activity?.id,
            name = inspectionTeam.name,
            routeUrl = inspectionTeam.routeUrl,
            scheduleUrl = inspectionTeam.scheduleUrl,
            routeNode = inspectionTeam.routeNode,
            eventArrangements = inspectionTeam.eventArrangements,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val inspectionTeams = inspectionTeamService.findByActivityId(activityId)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = inspectionTeams.map { inspectionTeam ->
            Response(
                id = inspectionTeam.id,
                activityId = inspectionTeam.activity?.id,
                name = inspectionTeam.name,
                routeUrl = inspectionTeam.routeUrl,
                scheduleUrl = inspectionTeam.scheduleUrl,
                routeNode = inspectionTeam.routeNode,
                eventArrangements = inspectionTeam.eventArrangements,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-name-containing")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<ApiResponse> {
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val inspectionTeams = inspectionTeamService.findByNameContaining(name)

        data class Response(
            val id: Long?,
            val activityId: Long?,
            val name: String?,
            val routeUrl: String?,
            val scheduleUrl: String?,
            val routeNode: List<String>,
            val eventArrangements: List<EventArrangementsItem>,
        )

        val rs = inspectionTeams.map { inspectionTeam ->
            Response(
                id = inspectionTeam.id,
                activityId = inspectionTeam.activity?.id,
                name = inspectionTeam.name,
                routeUrl = inspectionTeam.routeUrl,
                scheduleUrl = inspectionTeam.scheduleUrl,
                routeNode = inspectionTeam.routeNode,
                eventArrangements = inspectionTeam.eventArrangements,
            )
        }

        return builder.ok().data(rs).build()
    }
}
