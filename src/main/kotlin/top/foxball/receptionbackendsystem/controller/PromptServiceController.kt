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
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.service.PromptServiceService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/prompt-services")
class PromptServiceController(
    private val promptServiceService: PromptServiceService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: PromptService): ResponseEntity<Response> =
        ok(promptServiceService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<PromptService>): ResponseEntity<Response> =
        ok(promptServiceService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: PromptService): ResponseEntity<Response> =
        ok(promptServiceService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<PromptService>): ResponseEntity<Response> =
        ok(promptServiceService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        deleteById(request.id, promptServiceService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, promptServiceService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        findById(request.id, promptServiceService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(promptServiceService.findByActivityId(activityId))
    }
}
