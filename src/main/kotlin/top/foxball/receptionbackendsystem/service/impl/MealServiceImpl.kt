package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.MealRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.MealService

@Service
class MealServiceImpl(
    private val mealRepository: MealRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Meal, Int>(mealRepository), MealService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Meal> =
        mealRepository.findByActivityId(activityId)

    @Transactional
    override fun saveByActivity(activityId: Long, meals: List<Meal>): List<Meal> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingMealsById = activity.mealList.mapNotNull { meal ->
            meal.id?.let { it to meal }
        }.toMap()

        val normalizedMeals = meals.map { meal ->
            val targetMeal = meal.id?.let(existingMealsById::get) ?: Meal()
            targetMeal.activity = activity
            targetMeal.name = meal.name
            targetMeal.description = meal.description
            targetMeal.position = meal.position
            targetMeal.time = meal.time
            targetMeal
        }

        activity.mealList.clear()
        activity.mealList.addAll(normalizedMeals)

        return activitiesRepository.saveAndFlush(activity).mealList
    }
}
