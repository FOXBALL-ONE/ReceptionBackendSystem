package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.CarRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.CarService

@Service
class CarServiceImpl(
    private val carRepository: CarRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Car, Int>(carRepository), CarService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Car> =
        carRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndCarNumber(activityId: Long, carNumber: Long): Car? =
        carRepository.findByActivityIdAndCarNumber(activityId, carNumber)

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
