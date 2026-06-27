package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.InspectionPointSaveRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.service.InspectionPointService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 考察点控制器，挂在 /api/inspection-points 下。
 *
 * 提供按活动整体保存、单条/批量保存与更新、删除、按活动/ id 查询等考察点的增删改查端点。
 */
@RestController
@RequestMapping("/api/inspection-points")
class InspectionPointController(
    private val inspectionPointService: InspectionPointService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 按活动整体保存考察点，覆盖该活动下既有记录。 */
    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: InspectionPointSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val inspectionPoints = inspectionPointService.saveByActivity(activityId, request.inspectionPoints)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = inspectionPoints.map { inspectionPoint ->
            Response(
                id = inspectionPoint.id,
                activityId = inspectionPoint.activity?.id,
                imageURL = inspectionPoint.imageURL,
                description = inspectionPoint.description,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 新增单条考察点并返回。 */
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: InspectionPoint): ResponseEntity<ApiResponse> {
        val inspectionPoint = inspectionPointService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = Response(
            id = inspectionPoint.id,
            activityId = inspectionPoint.activity?.id,
            imageURL = inspectionPoint.imageURL,
            description = inspectionPoint.description,
        )

        return builder.ok().data(rs).build()
    }

    /** 批量新增考察点并返回。 */
    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<InspectionPoint>): ResponseEntity<ApiResponse> {
        val inspectionPoints = inspectionPointService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = inspectionPoints.map { inspectionPoint ->
            Response(
                id = inspectionPoint.id,
                activityId = inspectionPoint.activity?.id,
                imageURL = inspectionPoint.imageURL,
                description = inspectionPoint.description,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 更新单条考察点并返回。 */
    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: InspectionPoint): ResponseEntity<ApiResponse> {
        val inspectionPoint = inspectionPointService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = Response(
            id = inspectionPoint.id,
            activityId = inspectionPoint.activity?.id,
            imageURL = inspectionPoint.imageURL,
            description = inspectionPoint.description,
        )

        return builder.ok().data(rs).build()
    }

    /** 批量更新考察点并返回。 */
    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<InspectionPoint>): ResponseEntity<ApiResponse> {
        val inspectionPoints = inspectionPointService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = inspectionPoints.map { inspectionPoint ->
            Response(
                id = inspectionPoint.id,
                activityId = inspectionPoint.activity?.id,
                imageURL = inspectionPoint.imageURL,
                description = inspectionPoint.description,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按 id 删除单条考察点。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, inspectionPointService)

    /** 按 id 列表批量删除考察点。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, inspectionPointService)

    /** 按 id 查询单条考察点，未找到返回 notFound。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val inspectionPoint = inspectionPointService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = Response(
            id = inspectionPoint.id,
            activityId = inspectionPoint.activity?.id,
            imageURL = inspectionPoint.imageURL,
            description = inspectionPoint.description,
        )

        return builder.ok().data(rs).build()
    }

    /** 按活动 id 查询该活动下所有考察点。 */
    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val inspectionPoints = inspectionPointService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val imageURL: String?,
            val description: String?,
        )

        val rs = inspectionPoints.map { inspectionPoint ->
            Response(
                id = inspectionPoint.id,
                activityId = inspectionPoint.activity?.id,
                imageURL = inspectionPoint.imageURL,
                description = inspectionPoint.description,
            )
        }

        return builder.ok().data(rs).build()
    }
}
