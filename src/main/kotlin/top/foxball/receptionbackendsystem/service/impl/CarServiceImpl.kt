package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.CarRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.CarService

/**
 * 车辆服务实现，操作 [Car] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的车辆列表：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的车辆及其登车人员快照。
 */
@Service
class CarServiceImpl(
    private val carRepository: CarRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Car, Int>(carRepository), CarService {
    /** 按活动查询其下全部车辆。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Car> =
        carRepository.findByActivityId(activityId)

    /** 按活动与车号定位单辆车辆。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndCarNumber(activityId: Long, carNumber: Long): Car? =
        carRepository.findByActivityIdAndCarNumber(activityId, carNumber)

    /**
     * 整体覆盖保存某活动的车辆列表。
     *
     * 步骤：1) 加载活动并按 id 建立既有车辆索引；
     * 2) 逐辆归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与车辆字段；
     *    登车名单按 [Car.passengersOnBoardList] 深拷贝重建，乘车名单以人员快照形式存入 [Car.passengersList]；
     * 3) 清空 [Activities.carList] 后整体替换为归一化结果并保存活动。
     */
    @Transactional
    override fun saveByActivity(activityId: Long, cars: List<Car>): List<Car> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingCarsById = activity.carList.mapNotNull { car ->
            car.id?.let { it to car }
        }.toMap()

        val normalizedCars = cars.map { car ->
            val targetCar = car.id?.let(existingCarsById::get) ?: Car()
            targetCar.activity = activity
            targetCar.carNumber = car.carNumber
            targetCar.carPlate = car.carPlate
            targetCar.driver = car.driver
            targetCar.driverNumber = car.driverNumber
            targetCar.passengersOnBoardList.clear()
            targetCar.passengersOnBoardList.addAll(car.passengersOnBoardList.map { it.copy() })
            targetCar.passengersList = car.passengersList.map { it.toJsonSnapshot() }.toMutableList()
            targetCar
        }

        activity.carList.clear()
        activity.carList.addAll(normalizedCars)

        return activitiesRepository.saveAndFlush(activity).carList
    }

    private fun Person.toJsonSnapshot(): Person =
        Person(
            id = id,
            name = name,
            unit = unit,
            nickName = nickName,
            inspectionTeamItemId = inspectionTeamItemId,
        )
}
