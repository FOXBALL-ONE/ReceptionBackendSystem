package top.foxball.receptionbackendsystem.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.controller.request.InspectionTeamItemDto
import top.foxball.receptionbackendsystem.controller.request.InspectionTeamItineraryDto
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.service.InspectionTeamService
import top.foxball.receptionbackendsystem.service.ScheduleService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class ScheduleSaveByActivityTest {
    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var inspectionTeamService: InspectionTeamService

    @Test
    @Transactional
    fun `same inspection team keeps one identity across days with separate itineraries`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val startTime = LocalDateTime.now().withSecond(0).withNano(0)

        // 1. 先存两天
        val savedDays = scheduleService.saveByActivity(
            activityId = activityId,
            schedules = listOf(
                Schedule(scheduleTags = "第一天,接口测试,上午"),
                Schedule(scheduleTags = "第二天,接口测试,全天"),
            ),
        )
        assertEquals(2, savedDays.size)
        val day1Id = assertNotNull(savedDays[0].id)
        val day2Id = assertNotNull(savedDays[1].id)
        assertNotEquals(day1Id, day2Id)

        // 2. 存一个考察组，两天各有独立行程
        val savedGroups = inspectionTeamService.saveByActivity(
            activityId = activityId,
            groups = listOf(
                InspectionTeamItemDto(
                    name = "接口测试组",
                    itineraries = listOf(
                        InspectionTeamItineraryDto(
                            scheduleId = day1Id,
                            routeUrl = "/test/route-day1.pdf",
                            scheduleUrl = "/test/schedule-day1.pdf",
                            routeNode = listOf("第一天集合点", "第一天测试点位"),
                            eventArrangements = listOf(
                                EventArrangementsItem(
                                    startTime = startTime,
                                    endTime = startTime.plusHours(1),
                                    content = "第一天验证整体保存接口",
                                    location = "第一天测试会议室",
                                ),
                            ),
                        ),
                        InspectionTeamItineraryDto(
                            scheduleId = day2Id,
                            routeUrl = "/test/route-day2.pdf",
                            scheduleUrl = "/test/schedule-day2.pdf",
                            routeNode = listOf("第二天集合点", "第二天测试点位"),
                            eventArrangements = listOf(
                                EventArrangementsItem(
                                    startTime = startTime.plusDays(1),
                                    endTime = startTime.plusDays(1).plusHours(1),
                                    content = "第二天验证整体保存接口",
                                    location = "第二天测试会议室",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(1, savedGroups.size)
        val team = savedGroups.first()
        val teamId = assertNotNull(team.id)
        assertEquals(2, team.itineraries.size)
        // 两天的行程分别挂在不同的天，且内容各异
        val day1Itinerary = team.itineraries.first { it.schedule?.id == day1Id }
        val day2Itinerary = team.itineraries.first { it.schedule?.id == day2Id }
        assertNotEquals(day1Itinerary.id, day2Itinerary.id)
        assertEquals(listOf("第一天集合点", "第一天测试点位"), day1Itinerary.routeNode)
        assertEquals(listOf("第二天集合点", "第二天测试点位"), day2Itinerary.routeNode)

        // 3. 重复保存同一身份（带 id）应更新而非新建，行程按 id 更新
        val updatedGroups = inspectionTeamService.saveByActivity(
            activityId = activityId,
            groups = listOf(
                InspectionTeamItemDto(
                    id = teamId,
                    name = "接口测试更新组",
                    itineraries = listOf(
                        InspectionTeamItineraryDto(
                            id = day1Itinerary.id,
                            scheduleId = day1Id,
                            routeUrl = "/test/route-day1-updated.pdf",
                            scheduleUrl = "/test/schedule-day1-updated.pdf",
                            routeNode = listOf("更新集合点"),
                            eventArrangements = listOf(
                                EventArrangementsItem(
                                    startTime = startTime.plusHours(2),
                                    endTime = startTime.plusHours(3),
                                    content = "验证已有主键更新",
                                    location = "更新测试会议室",
                                ),
                            ),
                        ),
                        InspectionTeamItineraryDto(
                            id = day2Itinerary.id,
                            scheduleId = day2Id,
                            routeUrl = "/test/route-day2-updated.pdf",
                            scheduleUrl = "/test/schedule-day2-updated.pdf",
                            routeNode = listOf("第二天更新集合点"),
                            eventArrangements = listOf(
                                EventArrangementsItem(
                                    startTime = startTime.plusDays(1).plusHours(2),
                                    endTime = startTime.plusDays(1).plusHours(3),
                                    content = "第二天验证已有主键更新",
                                    location = "第二天更新测试会议室",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(1, updatedGroups.size)
        assertEquals(teamId, updatedGroups.first().id, "考察组身份 id 应保持不变")
        assertEquals("接口测试更新组", updatedGroups.first().name)
        assertEquals(2, updatedGroups.first().itineraries.size)

        // 4. 重查确认仍是单一身份、两天行程独立
        val reloadedGroups = inspectionTeamService.findByActivityId(activityId)
        assertEquals(1, reloadedGroups.size)
        assertEquals(teamId, reloadedGroups.first().id)
        assertEquals(2, reloadedGroups.first().itineraries.size)

        val reloadedDays = scheduleService.findByActivityId(activityId)
        assertEquals(2, reloadedDays.size)
    }
}
