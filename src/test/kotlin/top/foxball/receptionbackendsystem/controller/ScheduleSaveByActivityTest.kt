package top.foxball.receptionbackendsystem.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.service.ScheduleService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class ScheduleSaveByActivityTest {
    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Test
    @Transactional
    fun `save by activity replaces schedules`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val startTime = LocalDateTime.now().withSecond(0).withNano(0)

        val savedSchedules = scheduleService.saveByActivity(
            activityId = activityId,
            schedules = listOf(
                Schedule(
                    scheduleTags = "接口测试,整体保存",
                    inspectionTeamItem = mutableListOf(
                        InspectionTeamItem(
                            name = "接口测试组",
                            routeUrl = "/test/route.pdf",
                            scheduleUrl = "/test/schedule.pdf",
                            routeNode = mutableListOf("集合点", "测试点位"),
                            eventArrangements = mutableListOf(
                                EventArrangementsItem(
                                    startTime = startTime,
                                    endTime = startTime.plusHours(1),
                                    content = "验证整体保存接口",
                                    location = "测试会议室",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(1, savedSchedules.size)
        assertEquals(activityId, savedSchedules.first().activity?.id)
        assertEquals("接口测试,整体保存", savedSchedules.first().scheduleTags)
        assertEquals(1, savedSchedules.first().inspectionTeamItem.size)
        assertEquals("接口测试组", savedSchedules.first().inspectionTeamItem.first().name)

        val scheduleId = assertNotNull(savedSchedules.first().id)
        val inspectionTeamId = assertNotNull(savedSchedules.first().inspectionTeamItem.first().id)
        val updatedSchedules = scheduleService.saveByActivity(
            activityId = activityId,
            schedules = listOf(
                Schedule(
                    id = scheduleId,
                    scheduleTags = "接口测试,更新保存",
                    inspectionTeamItem = mutableListOf(
                        InspectionTeamItem(
                            id = inspectionTeamId,
                            name = "接口测试更新组",
                            routeUrl = "/test/route-updated.pdf",
                            scheduleUrl = "/test/schedule-updated.pdf",
                            routeNode = mutableListOf("更新集合点", "更新测试点位"),
                            eventArrangements = mutableListOf(
                                EventArrangementsItem(
                                    startTime = startTime.plusHours(2),
                                    endTime = startTime.plusHours(3),
                                    content = "验证已有主键更新",
                                    location = "更新测试会议室",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(scheduleId, updatedSchedules.first().id)
        assertEquals(inspectionTeamId, updatedSchedules.first().inspectionTeamItem.first().id)
        assertEquals("接口测试,更新保存", updatedSchedules.first().scheduleTags)
        assertEquals("接口测试更新组", updatedSchedules.first().inspectionTeamItem.first().name)

        val reloadedSchedules = scheduleService.findByActivityId(activityId)
        assertEquals(1, reloadedSchedules.size)
        assertEquals("接口测试,更新保存", reloadedSchedules.first().scheduleTags)
    }
}
