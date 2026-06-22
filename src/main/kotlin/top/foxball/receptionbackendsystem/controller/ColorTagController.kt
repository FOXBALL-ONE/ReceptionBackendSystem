package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityNameRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.service.ColorTagService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/color-tags")
class ColorTagController(
    private val colorTagService: ColorTagService,
    responseBuilder: ResponseBuilder,
) : ControllerSupport(responseBuilder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: ColorTag): ResponseEntity<Response> = ok(colorTagService.saveOne(entity))

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<Response> =
        ok(colorTagService.saveBatch(request.items))

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: ColorTag): ResponseEntity<Response> = ok(colorTagService.updateOne(entity))

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<Response> =
        ok(colorTagService.updateBatch(request.items))

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        deleteById(request.id, colorTagService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<Response> =
        deleteByIds(request.ids, colorTagService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        findById(request.id, colorTagService)

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        return ok(colorTagService.findByActivityId(activityId))
    }

    @PostMapping("/find-by-name")
    fun findByName(@RequestBody request: ActivityNameRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        return ok(colorTagService.findByActivityIdAndName(activityId, name))
    }

    @PostMapping("/exists-by-name")
    fun existsByName(@RequestBody request: ActivityNameRequest): ResponseEntity<Response> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        return ok(colorTagService.existsByActivityIdAndName(activityId, name))
    }
}
