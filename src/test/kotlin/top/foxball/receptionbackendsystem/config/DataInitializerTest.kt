package top.foxball.receptionbackendsystem.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import kotlin.test.assertTrue

/**
 * 数据初始化测试。
 */
@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Test
    @Transactional
    fun `测试数据初始化是否成功`() {
        // 验证数据库中已有活动记录
        val activities = activitiesRepository.findAll()
        assertTrue(activities.isNotEmpty(), "应该至少有一个活动记录")

        val activity = activities.first()

        // 验证主活动信息
        assertTrue(activity.name?.isNotEmpty() == true, "活动名称不应为空")
        assertTrue(activity.url?.isNotEmpty() == true, "活动URL不应为空")

        // 验证附属记录
        assertTrue(activity.schedules.isNotEmpty(), "应该有日程记录")
        assertTrue(activity.inspectionTeamItemList.isNotEmpty(), "应该有考察组记录")
        assertTrue(activity.carList.isNotEmpty(), "应该有车辆记录")
        assertTrue(activity.personList.isNotEmpty(), "应该有人员记录")
        assertTrue(activity.imageList.isNotEmpty(), "应该有图片记录")
        assertTrue(activity.colorTagList.isNotEmpty(), "应该有颜色标签记录")
        assertTrue(activity.colorTagList.any { it.type == ColorTag.TYPE_PERSON }, "应该有人员颜色标签记录")
        assertTrue(activity.colorTagList.any { it.type == ColorTag.TYPE_LODGING }, "应该有住宿颜色标签记录")
        assertTrue(activity.promptServiceList.isNotEmpty(), "应该有提示服务记录")
        assertTrue(activity.mealList.isNotEmpty(), "应该有用餐安排记录")
        assertTrue(activity.hostedList.isNotEmpty(), "应该有住宿安排记录")
        assertTrue(activity.inspectionPoints.isNotEmpty(), "应该有考察点记录")
        assertTrue(activity.overviewOfTheCityAndCounty.isNotEmpty(), "应该有市县概况记录")

        println("数据初始化验证通过！")
        println("活动名称: ${activity.name}")
        println("日程数量: ${activity.schedules.size}")
        println("考察组数量: ${activity.inspectionTeamItemList.size}")
        println("人员数量: ${activity.personList.size}")
    }
}
