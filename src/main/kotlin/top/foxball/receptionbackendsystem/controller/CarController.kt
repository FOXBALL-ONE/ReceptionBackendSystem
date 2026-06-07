package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.service.CarService
import top.foxball.receptionbackendsystem.shared.Response
import top.foxball.receptionbackendsystem.shared.ResponseBuilder

/**
 * 活动车辆接口。
 */
@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService,
    private val responseBuilder: ResponseBuilder,
) {

    /** 查询全部车辆。 */
    @GetMapping
    fun findAll(): ResponseEntity<Response> = ok(carService.findAll())

    /** 根据主键查询车辆。 */
    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int): ResponseEntity<Response> = ok(carService.findById(id))

    /** 查询指定活动下的全部车辆。 */
    @GetMapping("/activity/{activityId}")
    fun findByActivityId(@PathVariable activityId: Int): ResponseEntity<Response> =
        ok(carService.findByActivityId(activityId))

    /** 查询指定活动下的指定车号车辆。 */
    @GetMapping("/activity/{activityId}/number/{carNumber}")
    fun findByActivityIdAndCarNumber(
        @PathVariable activityId: Int,
        @PathVariable carNumber: Long,
    ): ResponseEntity<Response> = ok(carService.findByActivityIdAndCarNumber(activityId, carNumber))

    /** 在指定活动下创建车辆。 */
    @PostMapping("/activity/{activityId}")
    fun create(
        @PathVariable activityId: Int,
        @RequestBody car: Car,
    ): ResponseEntity<Response> = ok(carService.create(activityId, car), "车辆创建成功")

    /** 更新车辆信息。 */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Int,
        @RequestBody car: Car,
    ): ResponseEntity<Response> = ok(carService.update(id, car), "车辆更新成功")

    /** 删除车辆。 */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<Response> {
        carService.delete(id)
        return ok(mapOf("id" to id), "车辆删除成功")
    }

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
