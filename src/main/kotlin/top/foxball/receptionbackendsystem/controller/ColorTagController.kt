package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityNameRequest
import top.foxball.receptionbackendsystem.controller.request.ColorTagSaveRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.service.ColorTagService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 颜色标签控制器，挂在 /api/color-tags 下。
 *
 * 提供单条/批量保存与更新、删除、按活动/名称查询、名称存在性校验等颜色标签的增删改查端点。
 */
@RestController
@RequestMapping("/api/color-tags")
class ColorTagController(
    private val colorTagService: ColorTagService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 按活动保存单条颜色标签并返回。 */
    @PostMapping("/save-one")
    fun saveOne(@RequestBody request: ColorTagSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTag = colorTagService.saveByActivity(activityId, listOf(request.toEntity())).first()

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
            type = colorTag.type,
        )

        return builder.ok().data(rs).build()
    }

    /** 按活动批量保存颜色标签并返回。 */
    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTags = colorTagService.saveByActivity(activityId, request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
                type = colorTag.type,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按活动更新单条颜色标签并返回。 */
    @PostMapping("/update-one")
    fun updateOne(@RequestBody request: ColorTagSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTag = colorTagService.saveByActivity(activityId, listOf(request.toEntity())).first()

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
            type = colorTag.type,
        )

        return builder.ok().data(rs).build()
    }

    /** 按活动批量更新颜色标签并返回。 */
    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<ColorTag>): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTags = colorTagService.saveByActivity(activityId, request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
                type = colorTag.type,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按 id 删除单条颜色标签。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, colorTagService)

    /** 按 id 列表批量删除颜色标签。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, colorTagService)

    /** 按 id 查询单条颜色标签，未找到返回 notFound。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val colorTag = colorTagService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = Response(
            id = colorTag.id,
            activityId = colorTag.activity?.id,
            name = colorTag.name,
            color = colorTag.color,
            type = colorTag.type,
        )

        return builder.ok().data(rs).build()
    }

    /** 按活动 id 查询颜色标签，可选按 type 过滤。 */
    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val colorTags = request.type
            ?.takeIf { it.isNotBlank() }
            ?.let { colorTagService.findByActivityIdAndType(activityId, it) }
            ?: colorTagService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = colorTags.map { colorTag ->
            Response(
                id = colorTag.id,
                activityId = colorTag.activity?.id,
                name = colorTag.name,
                color = colorTag.color,
                type = colorTag.type,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按活动 id 与名称查询单条颜色标签，可选叠加 type 精确匹配。 */
    @PostMapping("/find-by-name")
    fun findByName(@RequestBody request: ActivityNameRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val colorTag = request.type
            ?.takeIf { it.isNotBlank() }
            ?.let { colorTagService.findByActivityIdAndNameAndType(activityId, name, it) }
            ?: colorTagService.findByActivityIdAndName(activityId, name)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        val rs = colorTag?.let {
            Response(
                id = it.id,
                activityId = it.activity?.id,
                name = it.name,
                color = it.color,
                type = it.type,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 校验活动下指定名称的颜色标签是否存在，可选叠加 type 精确匹配。 */
    @PostMapping("/exists-by-name")
    fun existsByName(@RequestBody request: ActivityNameRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val exists = request.type
            ?.takeIf { it.isNotBlank() }
            ?.let { colorTagService.existsByActivityIdAndNameAndType(activityId, name, it) }
            ?: colorTagService.existsByActivityIdAndName(activityId, name)
        return ok(exists)
    }
}
