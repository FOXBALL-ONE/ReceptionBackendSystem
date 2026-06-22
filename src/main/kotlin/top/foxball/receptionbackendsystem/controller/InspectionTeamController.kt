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
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.service.InspectionTeamService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/inspection-teams")
class InspectionTeamController(
    private val inspectionTeamService: InspectionTeamService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<Response> =
        ok(inspectionTeamService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<Response> =
        ok(inspectionTeamService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: InspectionTeamItem): ResponseEntity<Response> =
        ok(inspectionTeamService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<InspectionTeamItem>): ResponseEntity<Response> =
        ok(inspectionTeamService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        deleteById(request.id, inspectionTeamService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, inspectionTeamService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        findById(request.id, inspectionTeamService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(inspectionTeamService.findByActivityId(activityId))
    }

    @PostMapping("/find-by-name-containing")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<Response> {
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        return ok(inspectionTeamService.findByNameContaining(name))
    }
}
