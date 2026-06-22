package top.foxball.receptionbackendsystem.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.service.MealService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class MealSaveByActivityTest {
    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Autowired
    private lateinit var mealService: MealService

    @Test
    @Transactional
    fun `save by activity replaces meals`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val mealTime = LocalDateTime.now().withSecond(0).withNano(0)

        val savedMeals = mealService.saveByActivity(
            activityId = activityId,
            meals = listOf(
                Meal(
                    name = "接口测试午餐",
                    description = "接口测试餐食说明",
                    position = "接口测试餐厅",
                    time = mealTime,
                ),
            ),
        )

        assertEquals(1, savedMeals.size)
        assertEquals(activityId, savedMeals.first().activity?.id)
        assertEquals("接口测试午餐", savedMeals.first().name)

        val mealId = assertNotNull(savedMeals.first().id)
        val updatedMeals = mealService.saveByActivity(
            activityId = activityId,
            meals = listOf(
                Meal(
                    id = mealId,
                    name = "接口测试晚餐",
                    description = "接口测试餐食说明-更新",
                    position = "接口测试宴会厅",
                    time = mealTime.plusHours(6),
                ),
            ),
        )

        assertEquals(mealId, updatedMeals.first().id)
        assertEquals("接口测试晚餐", updatedMeals.first().name)
        assertEquals("接口测试宴会厅", updatedMeals.first().position)

        val reloadedMeals = mealService.findByActivityId(activityId)
        assertEquals(1, reloadedMeals.size)
        assertEquals("接口测试晚餐", reloadedMeals.first().name)
    }
}
