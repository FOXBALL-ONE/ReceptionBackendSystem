package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.CarRepository
import top.foxball.receptionbackendsystem.service.CarService

@Service
class CarServiceImpl(
    private val carRepository: CarRepository,
) : AbstractReceptionService<Car, Int>(carRepository), CarService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Car> =
        carRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndCarNumber(activityId: Int, carNumber: Long): Car? =
        carRepository.findByActivityIdAndCarNumber(activityId, carNumber)
}
