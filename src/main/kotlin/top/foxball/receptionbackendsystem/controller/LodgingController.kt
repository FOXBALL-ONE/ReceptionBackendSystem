package top.foxball.receptionbackendsystem.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.controller.request.LodgingSaveRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.LodgingService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/lodgings")
class LodgingController(
    private val lodgingService: LodgingService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: LodgingSaveRequest): ResponseEntity<ApiResponse> {
        log.info(
            "收到住宿保存请求: activityId={}, colorTagCount={}, colorTagIds={}, lodgingCount={}, lodgingIds={}",
            request.activityId,
            request.colorTags.size,
            request.colorTags.sampleColorTagIds(),
            request.lodgings.size,
            request.lodgings.sampleLodgingIds(),
        )
        val activityId = request.activityId
        if (activityId == null) {
            log.warn(
                "住宿保存请求被拒绝: 缺少 activityId, colorTagCount={}, lodgingCount={}",
                request.colorTags.size,
                request.lodgings.size,
            )
            return badRequest("activityId is required")
        }

        val result = lodgingService.saveByActivity(activityId, request.colorTags, request.lodgings)
        log.info(
            "住宿保存完成: activityId={}, savedColorTagCount={}, savedColorTagIds={}, savedLodgingCount={}, savedLodgingIds={}",
            activityId,
            result.colorTags.size,
            result.colorTags.sampleColorTagIds(),
            result.lodgings.size,
            result.lodgings.sampleLodgingIds(),
        )

        data class ColorTagResponse(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val color: String?,
            val type: String?,
        )

        data class LodgingResponse(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        data class Response(
            val colorTags: List<ColorTagResponse>,
            val lodgings: List<LodgingResponse>,
        )

        val rs = Response(
            colorTags = result.colorTags.map { colorTag ->
                ColorTagResponse(
                    id = colorTag.id,
                    activityId = colorTag.activity?.id,
                    name = colorTag.name,
                    color = colorTag.color,
                    type = colorTag.type,
                )
            },
            lodgings = result.lodgings.map { lodging ->
                LodgingResponse(
                    id = lodging.id,
                    activityId = lodging.activity?.id,
                    roomNumber = lodging.roomNumber,
                    person = lodging.person,
                    colorTag = lodging.colorTag,
                    hostedColorsTag = lodging.hostedColorsTag,
                )
            },
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Lodging): ResponseEntity<ApiResponse> {
        val lodging = lodgingService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = Response(
            id = lodging.id,
            activityId = lodging.activity?.id,
            roomNumber = lodging.roomNumber,
            person = lodging.person,
            colorTag = lodging.colorTag,
            hostedColorsTag = lodging.hostedColorsTag,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Lodging>): ResponseEntity<ApiResponse> {
        val lodgings = lodgingService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = lodgings.map { lodging ->
            Response(
                id = lodging.id,
                activityId = lodging.activity?.id,
                roomNumber = lodging.roomNumber,
                person = lodging.person,
                colorTag = lodging.colorTag,
                hostedColorsTag = lodging.hostedColorsTag,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Lodging): ResponseEntity<ApiResponse> {
        val lodging = lodgingService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = Response(
            id = lodging.id,
            activityId = lodging.activity?.id,
            roomNumber = lodging.roomNumber,
            person = lodging.person,
            colorTag = lodging.colorTag,
            hostedColorsTag = lodging.hostedColorsTag,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Lodging>): ResponseEntity<ApiResponse> {
        val lodgings = lodgingService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = lodgings.map { lodging ->
            Response(
                id = lodging.id,
                activityId = lodging.activity?.id,
                roomNumber = lodging.roomNumber,
                person = lodging.person,
                colorTag = lodging.colorTag,
                hostedColorsTag = lodging.hostedColorsTag,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, lodgingService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, lodgingService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val lodging = lodgingService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = Response(
            id = lodging.id,
            activityId = lodging.activity?.id,
            roomNumber = lodging.roomNumber,
            person = lodging.person,
            colorTag = lodging.colorTag,
            hostedColorsTag = lodging.hostedColorsTag,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        log.info("收到住宿列表请求: activityId={}", request.activityId)
        val activityId = request.activityId
        if (activityId == null) {
            log.warn("住宿列表请求被拒绝: 缺少 activityId")
            return badRequest("activityId is required")
        }

        val lodgings = lodgingService.findByActivityId(activityId)
        log.info(
            "住宿列表查询完成: activityId={}, lodgingCount={}, lodgingIds={}",
            activityId,
            lodgings.size,
            lodgings.sampleLodgingIds(),
        )

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val roomNumber: String?,
            val person: Person?,
            val colorTag: ColorTag?,
            val hostedColorsTag: ColorTag?,
        )

        val rs = lodgings.map { lodging ->
            Response(
                id = lodging.id,
                activityId = lodging.activity?.id,
                roomNumber = lodging.roomNumber,
                person = lodging.person,
                colorTag = lodging.colorTag,
                hostedColorsTag = lodging.hostedColorsTag,
            )
        }

        return builder.ok().data(rs).build()
    }

    private fun List<Lodging>.sampleLodgingIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    private fun List<ColorTag>.sampleColorTagIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    companion object {
        private const val LOG_SAMPLE_SIZE = 10
    }
}
