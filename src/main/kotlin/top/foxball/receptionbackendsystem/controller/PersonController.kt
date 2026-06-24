package top.foxball.receptionbackendsystem.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityUnitRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.controller.request.NameRequest
import top.foxball.receptionbackendsystem.controller.request.PersonSaveRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.PersonService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/persons")
class PersonController(
    private val personService: PersonService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: PersonSaveRequest): ResponseEntity<ApiResponse> {
        log.info(
            "收到人员保存请求: activityId={}, personCount={}, personIds={}",
            request.activityId,
            request.persons.size,
            request.persons.sampleIds(),
        )
        val activityId = request.activityId
        if (activityId == null) {
            log.warn("人员保存请求被拒绝: 缺少 activityId, personCount={}", request.persons.size)
            return badRequest("activityId is required")
        }

        val persons = personService.saveByActivity(activityId, request.persons)
        log.info(
            "人员保存完成: activityId={}, savedPersonCount={}, savedPersonIds={}",
            activityId,
            persons.size,
            persons.sampleIds(),
        )

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Person): ResponseEntity<ApiResponse> {
        val person = personService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = Response(
            id = person.id,
            activityId = person.activity?.id,
            name = person.name,
            unit = person.unit,
            nickName = person.nickName,
            colorTag = person.colorTag,
            inspectionTeamItemId = person.inspectionTeamItemId,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Person>): ResponseEntity<ApiResponse> {
        val persons = personService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Person): ResponseEntity<ApiResponse> {
        val person = personService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = Response(
            id = person.id,
            activityId = person.activity?.id,
            name = person.name,
            unit = person.unit,
            nickName = person.nickName,
            colorTag = person.colorTag,
            inspectionTeamItemId = person.inspectionTeamItemId,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Person>): ResponseEntity<ApiResponse> {
        val persons = personService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, personService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, personService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val person = personService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = Response(
            id = person.id,
            activityId = person.activity?.id,
            name = person.name,
            unit = person.unit,
            nickName = person.nickName,
            colorTag = person.colorTag,
            inspectionTeamItemId = person.inspectionTeamItemId,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        log.info("收到人员列表请求: activityId={}", request.activityId)
        val activityId = request.activityId
        if (activityId == null) {
            log.warn("人员列表请求被拒绝: 缺少 activityId")
            return badRequest("activityId is required")
        }

        val persons = personService.findByActivityId(activityId)
        log.info(
            "人员列表查询完成: activityId={}, personCount={}, personIds={}",
            activityId,
            persons.size,
            persons.sampleIds(),
        )

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-name-containing")
    fun findByNameContaining(@RequestBody request: NameRequest): ResponseEntity<ApiResponse> {
        val name = request.name?.takeIf { it.isNotBlank() } ?: return badRequest("name is required")
        val persons = personService.findByNameContaining(name)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-unit")
    fun findByUnit(@RequestBody request: ActivityUnitRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val unit = request.unit?.takeIf { it.isNotBlank() } ?: return badRequest("unit is required")
        val persons = personService.findByActivityIdAndUnit(activityId, unit)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val unit: String?,
            val nickName: String?,
            val colorTag: ColorTag?,
            val inspectionTeamItemId: Long?,
        )

        val rs = persons.map { person ->
            Response(
                id = person.id,
                activityId = person.activity?.id,
                name = person.name,
                unit = person.unit,
                nickName = person.nickName,
                colorTag = person.colorTag,
                inspectionTeamItemId = person.inspectionTeamItemId,
            )
        }

        return builder.ok().data(rs).build()
    }

    private fun List<Person>.sampleIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    companion object {
        private const val LOG_SAMPLE_SIZE = 10
    }
}
