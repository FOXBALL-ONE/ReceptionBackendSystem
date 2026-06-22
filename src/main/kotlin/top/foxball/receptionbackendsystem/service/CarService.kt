package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Car

interface CarService : ActivityEntityService<Car, Int> {
    fun findByActivityIdAndCarNumber(activityId: Long, carNumber: Long): Car?
}
