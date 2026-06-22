package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 用餐安排数据仓库。
 */
interface MealRepository : ReceptionRepository<Meal, Int> {
    /**
     * 查询指定活动下的全部用餐安排。
     */
    fun findByActivityId(activityId: Long): List<Meal>
}
