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
import top.foxball.receptionbackendsystem.controller.request.UrlRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.service.ActivitiesService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/activities")
class ActivitiesController(
    private val activitiesService: ActivitiesService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Activities): ResponseEntity<Response> =
        ok(activitiesService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Activities>): ResponseEntity<Response> =
        ok(activitiesService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Activities): ResponseEntity<Response> =
        ok(activitiesService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Activities>): ResponseEntity<Response> =
        ok(activitiesService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        deleteById(request.id, activitiesService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, activitiesService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<Response> =
        findById(request.id, activitiesService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(activitiesService.findByActivityId(activityId))
    }

    @PostMapping("/find-by-url")
    fun findByUrl(@RequestBody request: UrlRequest): ResponseEntity<Response> {
        val url = request.url?.takeIf { it.isNotBlank() } ?: return badRequest("url is required")
        return ok(activitiesService.findByUrl(url))
    }

    @PostMapping("/exists-by-url")
    fun existsByUrl(@RequestBody request: UrlRequest): ResponseEntity<Response> {
        val url = request.url?.takeIf { it.isNotBlank() } ?: return badRequest("url is required")
        return ok(activitiesService.existsByUrl(url))
    }

    @PostMapping("/find-open")
    fun findOpen(): ResponseEntity<Response> =
        ok(activitiesService.findOpenActivities())
}
