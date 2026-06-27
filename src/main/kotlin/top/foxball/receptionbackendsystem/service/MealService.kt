package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Meal

/**
 * 用餐安排业务服务契约。
 *
 * 为 [Meal] 提供按活动维度查询及整体覆盖保存用餐安排。
 */
interface MealService : ActivityEntityService<Meal, Int> {
    /**
     * 整体覆盖保存活动下的用餐安排。
     *
     * 以 [activityId] 为维度用 [meals] 覆盖该活动原有用餐记录。
     */
    fun saveByActivity(activityId: Long, meals: List<Meal>): List<Meal>
}
