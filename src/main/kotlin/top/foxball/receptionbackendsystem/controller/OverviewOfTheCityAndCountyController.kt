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
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.ParagraphContentItem
import top.foxball.receptionbackendsystem.service.OverviewOfTheCityAndCountyService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/overviews")
class OverviewOfTheCityAndCountyController(
    private val overviewService: OverviewOfTheCityAndCountyService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: OverviewOfTheCityAndCounty): ResponseEntity<ApiResponse> {
        val overview = overviewService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = Response(
            id = overview.id,
            activityId = overview.activity?.id,
            title = overview.title,
            topImageUrl = overview.topImageUrl,
            description = overview.description,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<OverviewOfTheCityAndCounty>): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val overviews = overviewService.saveByActivity(activityId, request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = overviews.map { overview ->
            Response(
                id = overview.id,
                activityId = overview.activity?.id,
                title = overview.title,
                topImageUrl = overview.topImageUrl,
                description = overview.description,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: OverviewOfTheCityAndCounty): ResponseEntity<ApiResponse> {
        val overview = overviewService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = Response(
            id = overview.id,
            activityId = overview.activity?.id,
            title = overview.title,
            topImageUrl = overview.topImageUrl,
            description = overview.description,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<OverviewOfTheCityAndCounty>): ResponseEntity<ApiResponse> {
        val overviews = overviewService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = overviews.map { overview ->
            Response(
                id = overview.id,
                activityId = overview.activity?.id,
                title = overview.title,
                topImageUrl = overview.topImageUrl,
                description = overview.description,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, overviewService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, overviewService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val overview = overviewService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = Response(
            id = overview.id,
            activityId = overview.activity?.id,
            title = overview.title,
            topImageUrl = overview.topImageUrl,
            description = overview.description,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val overviews = overviewService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val title: String?,
            val topImageUrl: String?,
            val description: List<ParagraphContentItem>,
        )

        val rs = overviews.map { overview ->
            Response(
                id = overview.id,
                activityId = overview.activity?.id,
                title = overview.title,
                topImageUrl = overview.topImageUrl,
                description = overview.description,
            )
        }

        return builder.ok().data(rs).build()
    }
}
