package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.MealRepository
import top.foxball.receptionbackendsystem.service.MealService

@Service
class MealServiceImpl(
    private val mealRepository: MealRepository,
) : AbstractReceptionService<Meal, Int>(mealRepository), MealService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Meal> =
        mealRepository.findByActivityId(activityId)
}
