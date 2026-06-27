package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.MealRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.MealService

/**
 * 用餐安排服务实现，操作 [Meal] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的用餐安排：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的记录。
 */
@Service
class MealServiceImpl(
    private val mealRepository: MealRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Meal, Int>(mealRepository), MealService {
    /** 按活动查询其下全部用餐安排。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Meal> =
        mealRepository.findByActivityId(activityId)

    /**
     * 整体覆盖保存某活动的用餐安排。
     *
     * 步骤：1) 加载活动并按 id 建立既有用餐项索引；
     * 2) 逐项归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与字段；
     * 3) 清空 [Activities.mealList] 后整体替换为归一化结果并保存活动。
     */
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
