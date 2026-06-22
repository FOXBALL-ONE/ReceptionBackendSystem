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
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.service.InspectionPointService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/inspection-points")
class InspectionPointController(
    private val inspectionPointService: InspectionPointService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
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

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, inspectionPointService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, inspectionPointService)

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
