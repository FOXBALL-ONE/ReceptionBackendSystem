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
import top.foxball.receptionbackendsystem.controller.request.PromptServiceSaveRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.NoteItem
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.StaffItem
import top.foxball.receptionbackendsystem.datasource.jdbc.WeatherItem
import top.foxball.receptionbackendsystem.service.PromptServiceService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/prompt-services")
class PromptServiceController(
    private val promptServiceService: PromptServiceService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: PromptServiceSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val promptService = promptServiceService.saveByActivity(activityId, request.toEntity())

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = Response(
            id = promptService.id,
            activityId = promptService.activity?.id,
            staffList = promptService.staffList,
            noteList = promptService.noteList,
            weatherList = promptService.weatherList,
            attendanceInstructionsMode = promptService.attendanceInstructionsMode,
            attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
            attendanceInstructionsContent = promptService.attendanceInstructionsContent,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: PromptService): ResponseEntity<ApiResponse> {
        val promptService = promptServiceService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = Response(
            id = promptService.id,
            activityId = promptService.activity?.id,
            staffList = promptService.staffList,
            noteList = promptService.noteList,
            weatherList = promptService.weatherList,
            attendanceInstructionsMode = promptService.attendanceInstructionsMode,
            attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
            attendanceInstructionsContent = promptService.attendanceInstructionsContent,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<PromptService>): ResponseEntity<ApiResponse> {
        val promptServices = promptServiceService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = promptServices.map { promptService ->
            Response(
                id = promptService.id,
                activityId = promptService.activity?.id,
                staffList = promptService.staffList,
                noteList = promptService.noteList,
                weatherList = promptService.weatherList,
                attendanceInstructionsMode = promptService.attendanceInstructionsMode,
                attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
                attendanceInstructionsContent = promptService.attendanceInstructionsContent,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: PromptService): ResponseEntity<ApiResponse> {
        val promptService = promptServiceService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = Response(
            id = promptService.id,
            activityId = promptService.activity?.id,
            staffList = promptService.staffList,
            noteList = promptService.noteList,
            weatherList = promptService.weatherList,
            attendanceInstructionsMode = promptService.attendanceInstructionsMode,
            attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
            attendanceInstructionsContent = promptService.attendanceInstructionsContent,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<PromptService>): ResponseEntity<ApiResponse> {
        val promptServices = promptServiceService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = promptServices.map { promptService ->
            Response(
                id = promptService.id,
                activityId = promptService.activity?.id,
                staffList = promptService.staffList,
                noteList = promptService.noteList,
                weatherList = promptService.weatherList,
                attendanceInstructionsMode = promptService.attendanceInstructionsMode,
                attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
                attendanceInstructionsContent = promptService.attendanceInstructionsContent,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, promptServiceService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, promptServiceService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val promptService = promptServiceService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = Response(
            id = promptService.id,
            activityId = promptService.activity?.id,
            staffList = promptService.staffList,
            noteList = promptService.noteList,
            weatherList = promptService.weatherList,
            attendanceInstructionsMode = promptService.attendanceInstructionsMode,
            attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
            attendanceInstructionsContent = promptService.attendanceInstructionsContent,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val promptServices = promptServiceService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val staffList: List<StaffItem>,
            val noteList: List<NoteItem>,
            val weatherList: List<WeatherItem>,
            val attendanceInstructionsMode: Boolean?,
            val attendanceInstructionsTitle: String?,
            val attendanceInstructionsContent: String?,
        )

        val rs = promptServices.map { promptService ->
            Response(
                id = promptService.id,
                activityId = promptService.activity?.id,
                staffList = promptService.staffList,
                noteList = promptService.noteList,
                weatherList = promptService.weatherList,
                attendanceInstructionsMode = promptService.attendanceInstructionsMode,
                attendanceInstructionsTitle = promptService.attendanceInstructionsTitle,
                attendanceInstructionsContent = promptService.attendanceInstructionsContent,
            )
        }

        return builder.ok().data(rs).build()
    }
}
