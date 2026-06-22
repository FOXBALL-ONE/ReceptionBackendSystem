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
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/color-tags")
class ColorTagController(
    private val colorTagService: ColorTagService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: ColorTag): ResponseEntity<ApiResponse> {
        val colorTag = colorTagService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<ApiResponse> {
        val colorTags = colorTagService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: ColorTag): ResponseEntity<ApiResponse> {
        val colorTag = colorTagService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<ApiResponse> {
        val colorTags = colorTagService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, colorTagService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, colorTagService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val colorTag = colorTagService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTags = colorTagService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-name")
    fun findByName(@RequestBody request: ActivityNameRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val colorTag = colorTagService.findByActivityIdAndName(activityId, name)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
        )

        val rs = colorTag?.let {
            Response(
                id = it.id,
                activityId = it.activity?.id,
                name = it.name,
                color = it.color,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/exists-by-name")
    fun existsByName(@RequestBody request: ActivityNameRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        return ok(colorTagService.existsByActivityIdAndName(activityId, name))
    }
}
