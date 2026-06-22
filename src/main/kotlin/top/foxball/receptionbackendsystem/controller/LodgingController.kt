package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.service.LodgingService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/lodgings")
class LodgingController(
    private val lodgingService: LodgingService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Lodging): ResponseEntity<Response> = ok(lodgingService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Lodging>): ResponseEntity<Response> =
        ok(lodgingService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Lodging): ResponseEntity<Response> = ok(lodgingService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Lodging>): ResponseEntity<Response> =
        ok(lodgingService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        deleteById(request.id, lodgingService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, lodgingService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        findById(request.id, lodgingService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(lodgingService.findByActivityId(activityId))
    }
}
