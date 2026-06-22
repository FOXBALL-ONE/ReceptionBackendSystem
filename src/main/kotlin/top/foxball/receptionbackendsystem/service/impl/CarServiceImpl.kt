package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.CarRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.CarService

/**
 * 活动车辆业务服务实现。
 */
@Service
@Transactional
class CarServiceImpl(
    private val carRepository: CarRepository,
    private val activitiesRepository: ActivitiesRepository,
) : CarService {

    /** 查询全部车辆记录。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<Car> = carRepository.findAll()

    /** 根据主键查询车辆记录。 */
    @Transactional(readOnly = true)
    override fun findById(id: Int): Car = carRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("车辆不存在：$id") }

    /** 查询指定活动下的车辆列表。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Car> = carRepository.findByActivityId(activityId)

    /** 查询指定活动下的指定车号车辆。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndCarNumber(activityId: Int, carNumber: Long): Car =
        carRepository.findByActivityIdAndCarNumber(activityId, carNumber)
            ?: throw ResourceNotFoundException("活动 $activityId 下不存在车号 $carNumber")

    /** 在指定活动下创建车辆记录。 */
    override fun create(activityId: Int, car: Car): Car {
        car.id = null
        car.activity = findActivity(activityId)
        return carRepository.save(car)
    }

    /** 更新车辆记录，并保留或重设所属活动关系。 */
    override fun update(id: Int, car: Car): Car {
        val existing = findById(id)
        val activity = car.activity?.id?.let(::findActivity)
            ?: existing.activity
            ?: throw ResourceNotFoundException("车辆所属活动不存在")

        car.id = id
        car.activity = activity
        return carRepository.save(car)
    }

    /** 删除车辆记录。 */
    override fun delete(id: Int) {
        if (!carRepository.existsById(id)) {
            throw ResourceNotFoundException("车辆不存在：$id")
        }

        carRepository.deleteById(id)
    }

    /** 批量保存车辆，先删除活动下的旧车辆，再批量插入新车辆。 */
    override fun batchSave(activityId: Int, cars: List<Car>): List<Car> {
        val activity = findActivity(activityId)

        // 删除该活动下的所有旧车辆
        val oldCars = carRepository.findByActivityId(activityId)
        carRepository.deleteAll(oldCars)

        // 批量插入新车辆
        val newCars = cars.map { car ->
            car.id = null
            car.activity = activity
            car
        }

        return carRepository.saveAll(newCars)
    }

    /** 查询车辆所属活动。 */
    private fun findActivity(activityId: Int): Activities = activitiesRepository.findById(activityId)
        .orElseThrow { ResourceNotFoundException("活动不存在：$activityId") }
}
