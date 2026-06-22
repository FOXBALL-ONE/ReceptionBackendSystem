package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityCarNumberRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.PassengersOnBoardItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.CarService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Car): ResponseEntity<ApiResponse> {
        val car = carService.saveOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = Response(
            id = car.id,
            activityId = car.activity?.id,
            carNumber = car.carNumber,
            carPlate = car.carPlate,
            driver = car.driver,
            driverNumber = car.driverNumber,
            passengersOnBoardList = car.passengersOnBoardList,
            passengersList = car.passengersList,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Car>): ResponseEntity<ApiResponse> {
        val cars = carService.saveBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = cars.map { car ->
            Response(
                id = car.id,
                activityId = car.activity?.id,
                carNumber = car.carNumber,
                carPlate = car.carPlate,
                driver = car.driver,
                driverNumber = car.driverNumber,
                passengersOnBoardList = car.passengersOnBoardList,
                passengersList = car.passengersList,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Car): ResponseEntity<ApiResponse> {
        val car = carService.updateOne(entity)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = Response(
            id = car.id,
            activityId = car.activity?.id,
            carNumber = car.carNumber,
            carPlate = car.carPlate,
            driver = car.driver,
            driverNumber = car.driverNumber,
            passengersOnBoardList = car.passengersOnBoardList,
            passengersList = car.passengersList,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Car>): ResponseEntity<ApiResponse> {
        val cars = carService.updateBatch(request.items)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = cars.map { car ->
            Response(
                id = car.id,
                activityId = car.activity?.id,
                carNumber = car.carNumber,
                carPlate = car.carPlate,
                driver = car.driver,
                driverNumber = car.driverNumber,
                passengersOnBoardList = car.passengersOnBoardList,
                passengersList = car.passengersList,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, carService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, carService)

    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val car = carService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = Response(
            id = car.id,
            activityId = car.activity?.id,
            carNumber = car.carNumber,
            carPlate = car.carPlate,
            driver = car.driver,
            driverNumber = car.driverNumber,
            passengersOnBoardList = car.passengersOnBoardList,
            passengersList = car.passengersList,
        )

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val cars = carService.findByActivityId(activityId)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = cars.map { car ->
            Response(
                id = car.id,
                activityId = car.activity?.id,
                carNumber = car.carNumber,
                carPlate = car.carPlate,
                driver = car.driver,
                driverNumber = car.driverNumber,
                passengersOnBoardList = car.passengersOnBoardList,
                passengersList = car.passengersList,
            )
        }

        return builder.ok().data(rs).build()
    }

    @PostMapping("/find-by-car-number")
    fun findByCarNumber(@RequestBody request: ActivityCarNumberRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val carNumber = request.carNumber ?: return badRequest("carNumber is required")
        val car = carService.findByActivityIdAndCarNumber(activityId, carNumber)

        data class Response(
            val id: Int?,
            val activityId: Long?,
            val carNumber: Long?,
            val carPlate: String?,
            val driver: String?,
            val driverNumber: String?,
            val passengersOnBoardList: List<PassengersOnBoardItem>,
            val passengersList: List<Person>,
        )

        val rs = car?.let {
            Response(
                id = it.id,
                activityId = it.activity?.id,
                carNumber = it.carNumber,
                carPlate = it.carPlate,
                driver = it.driver,
                driverNumber = it.driverNumber,
                passengersOnBoardList = it.passengersOnBoardList,
                passengersList = it.passengersList,
            )
        }

        return builder.ok().data(rs).build()
    }
}
