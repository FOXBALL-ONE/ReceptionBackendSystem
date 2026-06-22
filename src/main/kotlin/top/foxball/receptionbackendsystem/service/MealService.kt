package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Meal

interface MealService : ActivityEntityService<Meal, Int> {
    fun saveByActivity(activityId: Long, meals: List<Meal>): List<Meal>
}
