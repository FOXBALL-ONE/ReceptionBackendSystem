package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.ActivityCarNumberRequest
import top.foxball.receptionbackendsystem.controller.request.ActivityIdRequest
import top.foxball.receptionbackendsystem.controller.request.BatchSaveCarsRequest
import top.foxball.receptionbackendsystem.controller.request.CreateCarRequest
import top.foxball.receptionbackendsystem.controller.request.EmptyRequest
import top.foxball.receptionbackendsystem.controller.request.IntIdRequest
import top.foxball.receptionbackendsystem.controller.request.UpdateCarRequest
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
    @PostMapping("/list")
    fun findAll(@RequestBody(required = false) request: EmptyRequest?): ResponseEntity<Response> =
        ok(carService.findAll())

    /** 根据主键查询车辆。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: IntIdRequest): ResponseEntity<Response> =
        ok(carService.findById(request.id))

    /** 查询指定活动下的全部车辆。 */
    @PostMapping("/find-by-activity")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<Response> =
        ok(carService.findByActivityId(request.activityId))

    /** 查询指定活动下的指定车号车辆。 */
    @PostMapping("/find-by-activity-and-number")
    fun findByActivityIdAndCarNumber(@RequestBody request: ActivityCarNumberRequest): ResponseEntity<Response> =
        ok(carService.findByActivityIdAndCarNumber(request.activityId, request.carNumber))

    /** 在指定活动下创建车辆。 */
    @PostMapping("/create")
    fun create(@RequestBody request: CreateCarRequest): ResponseEntity<Response> =
        ok(carService.create(request.activityId, request.car), "车辆创建成功")

    /** 更新车辆信息。 */
    @PostMapping("/update")
    fun update(@RequestBody request: UpdateCarRequest): ResponseEntity<Response> =
        ok(carService.update(request.id, request.car), "车辆更新成功")

    /** 删除车辆。 */
    @PostMapping("/delete")
    fun delete(@RequestBody request: IntIdRequest): ResponseEntity<Response> {
        carService.delete(request.id)
        return ok(mapOf("id" to request.id), "车辆删除成功")
    }

    /** 批量保存车辆。 */
    @PostMapping("/save")
    fun batchSave(@RequestBody request: BatchSaveCarsRequest): ResponseEntity<Response> =
        ok(carService.batchSave(request.activityId, request.cars), "保存成功")

    /** 使用统一响应结构返回成功结果。 */
    private fun ok(data: Any?, message: String = "OK"): ResponseEntity<Response> =
        responseBuilder.ok().message(message).data(data).build()
}
