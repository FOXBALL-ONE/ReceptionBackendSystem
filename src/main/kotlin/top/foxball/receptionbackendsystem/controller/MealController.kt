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
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.service.MealService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/meals")
class MealController(
    private val mealService: MealService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Meal): ResponseEntity<ApiResponse> {
        val meal = mealService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = Response(
            id = meal.id,
            activityId = meal.activity?.id,
            name = meal.name,
            description = meal.description,
            position = meal.position,
            time = meal.time,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Meal>): ResponseEntity<ApiResponse> {
        val meals = mealService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = meals.map { meal ->
            Response(
                id = meal.id,
                activityId = meal.activity?.id,
                name = meal.name,
                description = meal.description,
                position = meal.position,
                time = meal.time,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Meal): ResponseEntity<ApiResponse> {
        val meal = mealService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = Response(
            id = meal.id,
            activityId = meal.activity?.id,
            name = meal.name,
            description = meal.description,
            position = meal.position,
            time = meal.time,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Meal>): ResponseEntity<ApiResponse> {
        val meals = mealService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = meals.map { meal ->
            Response(
                id = meal.id,
                activityId = meal.activity?.id,
                name = meal.name,
                description = meal.description,
                position = meal.position,
                time = meal.time,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, mealService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, mealService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val meal = mealService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = Response(
            id = meal.id,
            activityId = meal.activity?.id,
            name = meal.name,
            description = meal.description,
            position = meal.position,
            time = meal.time,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val meals = mealService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val name: String?,
            val description: String?,
            val position: String?,
            val time: LocalDateTime?,
        )

        val rs = meals.map { meal ->
            Response(
                id = meal.id,
                activityId = meal.activity?.id,
                name = meal.name,
                description = meal.description,
                position = meal.position,
                time = meal.time,
            )
        }

        return builder.ok().data(rs).build()
    }
}
