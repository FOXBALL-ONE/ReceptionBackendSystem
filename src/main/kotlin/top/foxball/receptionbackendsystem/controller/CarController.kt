package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityCarNumberRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.CarSaveRequest
import top.foxball.receptionbackendsystem.controller.request.EntityBatchRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdsRequest
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.PassengersOnBoardItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.CarService
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 车辆控制器，挂在 /api/cars 下。
 *
 * 提供按活动整体保存、单条/批量保存与更新、删除、按活动/车号查询等车辆增删改查端点。
 */
@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 按活动整体保存车辆，覆盖该活动下既有记录。 */
    @PostMapping("/save")
    fun saveByActivity(@RequestBody request: CarSaveRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val cars = carService.saveByActivity(activityId, request.cars)

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

    /** 新增单条车辆并返回。 */
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

    /** 批量新增车辆并返回。 */
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

    /** 更新单条车辆并返回。 */
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

    /** 批量更新车辆并返回。 */
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

    /** 按 id 删除单条车辆。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: IntIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, carService)

    /** 按 id 列表批量删除车辆。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: IntIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, carService)

    /** 按 id 查询单条车辆，未找到返回 notFound。 */
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

    /** 按活动 id 查询该活动下所有车辆。 */
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

    /** 按活动 id 与车号精确查询单条车辆。 */
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
