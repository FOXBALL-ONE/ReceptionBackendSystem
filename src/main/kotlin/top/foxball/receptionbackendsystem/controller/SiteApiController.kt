package top.foxball.receptionbackendsystem.controller

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.datasource.jdbc.*
import top.foxball.receptionbackendsystem.handlder.ForbiddenException
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 前台站点开放数据接口。
 *
 * 为前台展示页（`/s`）提供只读数据：每个端点同时挂在
 * `/api/site/{activityUrl}/...`（按 url 定位活动）与 `/api/...`（取首个开放活动）下。
 * 全程只读事务，活动未开放时返回禁止访问或访问标志。
 */
@RestController
@Transactional(readOnly = true)
class SiteApiController(
    private val activitiesService: ActivitiesService,
    private val scheduleService: ScheduleService,
    private val personService: PersonService,
    private val carService: CarService,
    private val mealService: MealService,
    private val lodgingService: LodgingService,
    private val inspectionPointService: InspectionPointService,
    private val promptServiceService: PromptServiceService,
    private val overviewOfTheCityAndCountyService: OverviewOfTheCityAndCountyService,
    private val inspectionTeamService: InspectionTeamService,
) {
    /** 活动元信息：标题、banner、ICP、考勤提示等；未开放时仅返回 access=false。 */
    @GetMapping("/api/site/{activityUrl}/meta", "/api/meta")
    fun meta(
        @PathVariable(required = false) activityUrl: String?,
    ): SiteMetaResponse {
        val activity = resolveActivity(activityUrl)
        val promptService = promptServiceService.findByActivityId(activity.requiredId()).firstOrNull()

        if (activity.isOpen == false) {
            return SiteMetaResponse(
                access = false,
                eventName = activity.name ?: activity.masterTitle,
                headerTitle = activity.masterTitle,
            )
        }

        return SiteMetaResponse(
            access = true,
            eventName = activity.name ?: activity.masterTitle,
            headerTitle = activity.masterTitle,
            heroLabel = activity.subtitle ?: firstTag(activity.bannerTags),
            dateRange = activity.dateRange(),
            bannerImage = activity.bannerUrl,
            icpNumber = activity.icp,
            techSupport = activity.technicalSupport,
            showLoading = activity.isAnimation,
            showMeetingLocation = true,
            serviceMode = if (promptService?.attendanceInstructionsMode == true) 1 else 0,
            attendNotice = promptService?.attendanceInstructionsContent,
            attendNoticeTitle = promptService?.attendanceInstructionsTitle,
        )
    }

    /** 日程：按天返回各组考察行程与会议安排。 */
    @GetMapping("/api/site/{activityUrl}/schedule", "/api/schedule")
    fun schedule(
        @PathVariable(required = false) activityUrl: String?,
    ): SiteScheduleResponse {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        val schedules = scheduleService.findByActivityId(activityId)
        // 考察组身份在活动级，行程按天分组：取当天各组的行程作为该天的分组安排。
        val dayItinerariesByScheduleId = inspectionTeamService.findByActivityId(activityId)
            .flatMap { team -> team.itineraries.map { team to it } }
            .filter { it.second.schedule?.id != null }
            .groupBy { it.second.schedule!!.id!! }

        return SiteScheduleResponse(
            days = schedules.mapIndexed { index, schedule ->
                schedule.toSiteScheduleDay(
                    index,
                    itineraries = dayItinerariesByScheduleId[schedule.id].orEmpty(),
                )
            },
        )
    }

    /** 人员名单：含颜色标签与所属考察组。 */
    @GetMapping("/api/site/{activityUrl}/people", "/api/people")
    fun people(
        @PathVariable(required = false) activityUrl: String?,
    ): List<SitePersonResponse> {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        val teamNames = inspectionTeamService.findByActivityId(activityId)
            .associate { it.id to it.name }

        return personService.findByActivityId(activityId).map { person ->
            val colorTag = person.colorTag
            SitePersonResponse(
                type = colorTag?.name ?: "People",
                name = person.name,
                unit = person.unit,
                group = person.inspectionTeamItemId?.let(teamNames::get),
                avatar = null,
                typeColor = colorTag?.color,
            )
        }
    }

    /** 车辆安排：车牌、司机与乘车人员。 */
    @GetMapping("/api/site/{activityUrl}/vehicles", "/api/vehicles")
    fun vehicles(
        @PathVariable(required = false) activityUrl: String?,
    ): List<SiteVehicleResponse> {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        return carService.findByActivityId(activityId).map { car ->
            SiteVehicleResponse(
                id = car.carNumber ?: car.id?.toLong(),
                plate = car.carPlate,
                driver = SiteContactResponse(
                    name = car.driver,
                    phone = car.driverNumber,
                ),
                passengers = car.passengersList.mapNotNull { it.name },
                leaders = car.passengersOnBoardList.map {
                    SiteContactResponse(
                        name = it.name,
                        phone = it.phone,
                    )
                },
            )
        }
    }

    /** 餐饮安排：时间、名称与备注。 */
    @GetMapping("/api/site/{activityUrl}/meals", "/api/meals")
    fun meals(
        @PathVariable(required = false) activityUrl: String?,
    ): List<SiteMealResponse> {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        return mealService.findByActivityId(activityId).map { meal ->
            SiteMealResponse(
                time = meal.time,
                label = meal.name,
                remark = meal.remark(),
            )
        }
    }

    /** 住宿安排：人员、单位与房号。 */
    @GetMapping("/api/site/{activityUrl}/hotel", "/api/hotel")
    fun hotel(
        @PathVariable(required = false) activityUrl: String?,
    ): List<SiteHotelResponse> {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        return lodgingService.findByActivityId(activityId).map { lodging ->
            val colorTag = lodging.colorTag
            SiteHotelResponse(
                type = colorTag?.name ?: "Lodging",
                typeColor = colorTag?.color,
                name = lodging.person?.displayName(),
                unit = lodging.person?.unit,
                room = lodging.roomNumber,
            )
        }
    }

    /** 考察点位：名称、简介与图片。 */
    @GetMapping("/api/site/{activityUrl}/sites", "/api/sites")
    fun sites(
        @PathVariable(required = false) activityUrl: String?,
    ): List<SiteInspectionPointResponse> {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        return inspectionPointService.findByActivityId(activityId).mapIndexed { index, point ->
            SiteInspectionPointResponse(
                name = point.siteName(index),
                intro = point.description,
                images = listOfNotNull(point.imageURL),
            )
        }
    }

    /** 服务信息：接待人员、天气与公告。 */
    @GetMapping("/api/site/{activityUrl}/service", "/api/service")
    fun service(
        @PathVariable(required = false) activityUrl: String?,
    ): SiteServiceResponse {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        val services = promptServiceService.findByActivityId(activityId)
        return SiteServiceResponse(
            staff = services.flatMap { prompt ->
                prompt.staffList.map { staff ->
                    SiteStaffGroupResponse(
                        type = staff.name,
                        members = staff.groupList.map { member ->
                            SiteContactResponse(
                                name = member.name,
                                phone = member.phone,
                                duty = member.duty,
                            )
                        },
                    )
                }
            },
            weather = services.flatMap { prompt ->
                prompt.weatherList.map { weather ->
                    SiteWeatherResponse(
                        date = weather.time?.format(DATE_FORMATTER),
                        city = weather.city,
                        desc = weather.weatherDescriptor,
                        description = weather.weatherDescriptor,
                        temp = weather.temperature,
                    )
                }
            },
            notices = services.flatMap { prompt ->
                prompt.noteList.map { note ->
                    SiteNoticeResponse(
                        title = note.title,
                        content = note.content,
                    )
                }
            },
        )
    }

    /** 城市概况：顶部图片与分段介绍。 */
    @GetMapping("/api/site/{activityUrl}/overview", "/api/overview")
    fun overview(
        @PathVariable(required = false) activityUrl: String?,
    ): SiteOverviewResponse {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        val overviews = overviewOfTheCityAndCountyService.findByActivityId(activityId)
        return SiteOverviewResponse(
            image = overviews.firstNotNullOfOrNull { it.topImageUrl },
            sections = overviews.flatMap { it.toSections() },
        )
    }

    /** 解析活动：url 为空时取首个（优先开放）活动，否则按 url 查找。 */
    private fun resolveActivity(activityUrl: String?): Activities {
        val normalizedUrl = activityUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() && it != "index-red.html" }

        return if (normalizedUrl == null) {
            activitiesService.findOpenActivities().firstOrNull()
                ?: activitiesService.findAllActivities().firstOrNull()
                ?: throw ResourceNotFoundException("activity not found")
        } else {
            activitiesService.findByUrl(normalizedUrl)
                ?: throw ResourceNotFoundException("activity not found")
        }
    }

    /** 解析活动并强制校验已开放，未开放抛 ForbiddenException。 */
    private fun resolveOpenActivity(activityUrl: String?): Activities {
        val activity = resolveActivity(activityUrl)
        if (activity.isOpen != true) {
            throw ForbiddenException("活动未开放")
        }
        return activity
    }

    private fun Activities.requiredId(): Long =
        id ?: throw ResourceNotFoundException("activity not found")

    /** 将日程天与其上的各组行程组装为前端用的日程天视图。 */
    private fun Schedule.toSiteScheduleDay(
        index: Int,
        itineraries: List<Pair<InspectionTeamItem, InspectionTeamItinerary>>,
    ): SiteScheduleDayResponse {
        val groups = itineraries.mapIndexed { groupIndex, (team, itinerary) ->
            itinerary.toSiteScheduleGroup(groupIndex, team.name)
        }

        val meetings = groups.flatMap { it.meetings }
        return SiteScheduleDayResponse(
            date = scheduleDateLabel(index, itineraries, meetings),
            groups = groups,
            meetings = meetings,
        )
    }

    /** 推算日程天日期标签：优先取首个会议时间，其次取标签或会议时间，回退 Day n。 */
    private fun Schedule.scheduleDateLabel(
        index: Int,
        itineraries: List<Pair<InspectionTeamItem, InspectionTeamItinerary>>,
        meetings: List<SiteMeetingResponse>,
    ): String {
        val firstMeetingTime = itineraries
            .flatMap { it.second.eventArrangements }
            .mapNotNull { it.startTime }
            .minOrNull()

        if (firstMeetingTime != null) {
            return firstMeetingTime.format(DATE_FORMATTER)
        }

        return firstTag(scheduleTags) ?: meetings.firstOrNull()?.time ?: "Day ${index + 1}"
    }

    /** 将考察组行程转换为前端分组视图（含路线图与会议列表）。 */
    private fun InspectionTeamItinerary.toSiteScheduleGroup(
        index: Int,
        teamName: String?,
    ): SiteScheduleGroupResponse =
        SiteScheduleGroupResponse(
            name = teamName ?: "Group ${index + 1}",
            mapImage = routeUrl?.takeIf { it.isLikelyImageUrl() },
            route = routeNode,
            meetings = eventArrangements
                .sortedWith(compareBy<EventArrangementsItem> { it.startTime }.thenBy { it.endTime })
                .map { it.toSiteMeeting() },
        )

    private fun EventArrangementsItem.toSiteMeeting(): SiteMeetingResponse =
        SiteMeetingResponse(
            time = timeRange(startTime, endTime),
            content = content,
            location = location,
        )

    /** 餐饮备注：将地点与描述合并为多行文本。 */
    private fun Meal.remark(): String? =
        listOfNotNull(position, description)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .takeIf { it.isNotBlank() }

    private fun Person.displayName(): String? =
        name?.takeIf { it.isNotBlank() } ?: name

    /** 取考察点位显示名：描述的首行非空文本（截断 24 字），回退 Site n。 */
    private fun InspectionPoint.siteName(index: Int): String =
        description
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotBlank() }
            ?.take(24)
            ?: "Site ${index + 1}"

    /** 将城市概况段落展开为前端分段视图。 */
    private fun OverviewOfTheCityAndCounty.toSections(): List<SiteOverviewSectionResponse> {
        if (description.isEmpty()) {
            return listOf(
                SiteOverviewSectionResponse(
                    title = title,
                    content = "",
                ),
            )
        }

        return description.map { paragraph ->
            SiteOverviewSectionResponse(
                title = paragraph.title ?: title,
                content = paragraph.content,
            )
        }
    }

    /** 活动起止日期区间文本（同天单日期、跨天用「 - 」连接）。 */
    private fun Activities.dateRange(): String? {
        val start = startTime
        val end = endTime
        return when {
            start == null && end == null -> null
            start != null && end == null -> start.format(DATE_FORMATTER)
            start == null && end != null -> end.format(DATE_FORMATTER)
            start!!.toLocalDate() == end!!.toLocalDate() -> start.format(DATE_FORMATTER)
            else -> "${start.format(DATE_FORMATTER)} - ${end.format(DATE_FORMATTER)}"
        }
    }

    private fun String.isLikelyImageUrl(): Boolean {
        val lower = substringBefore('?').lowercase()
        return lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".png") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".gif") ||
            lower.endsWith(".svg")
    }

    private fun String?.toRoomNumber(): String? =
        this
            ?.trim()
            ?.let { ROOM_NUMBER_REGEX.find(it)?.groupValues?.getOrNull(1) }
            ?.takeIf { it.isNotBlank() }

    private companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        private val ROOM_NUMBER_REGEX = Regex("""房号\s*([0-9A-Za-z\u4e00-\u9fa5-]+)""")

        private fun firstTag(tags: String?): String? =
            tags
                ?.split(',', '，', ';', '；')
                ?.map { it.trim() }
                ?.firstOrNull { it.isNotBlank() }

        private fun timeRange(start: LocalDateTime?, end: LocalDateTime?): String =
            when {
                start == null && end == null -> ""
                start != null && end == null -> start.format(TIME_FORMATTER)
                start == null && end != null -> end.format(TIME_FORMATTER)
                else -> "${start!!.format(TIME_FORMATTER)}-${end!!.format(TIME_FORMATTER)}"
            }
    }
}

/** 活动元信息响应 */
data class SiteMetaResponse(
    val access: Boolean,
    val eventName: String? = null,
    val headerTitle: String? = null,
    val heroLabel: String? = null,
    val dateRange: String? = null,
    val bannerImage: String? = null,
    val icpNumber: String? = null,
    val techSupport: String? = null,
    val showLoading: Boolean? = null,
    val showMeetingLocation: Boolean? = null,
    val serviceMode: Int = 0,
    val attendNotice: String? = null,
    val attendNoticeTitle: String? = null,
    val closedTitle: String? = null,
    val closedMessage: String? = null,
)

/** 日程响应 */
data class SiteScheduleResponse(
    val days: List<SiteScheduleDayResponse>,
)

/** 日程天响应 */
data class SiteScheduleDayResponse(
    val date: String,
    val groups: List<SiteScheduleGroupResponse>,
    val meetings: List<SiteMeetingResponse> = emptyList(),
)

/** 日程分组响应 */
data class SiteScheduleGroupResponse(
    val name: String,
    val mapImage: String? = null,
    val route: List<String> = emptyList(),
    val meetings: List<SiteMeetingResponse> = emptyList(),
)

/** 会议安排响应 */
data class SiteMeetingResponse(
    val time: String,
    val content: String?,
    val location: String?,
)

/** 人员响应 */
data class SitePersonResponse(
    val type: String,
    val name: String?,
    val unit: String?,
    val group: String?,
    val avatar: String?,
    val typeColor: String?,
)

/** 车辆响应 */
data class SiteVehicleResponse(
    val id: Long?,
    val plate: String?,
    val driver: SiteContactResponse?,
    val passengers: List<String>,
    val leaders: List<SiteContactResponse>,
)

/** 联系人响应 */
data class SiteContactResponse(
    val name: String?,
    val phone: String?,
    val duty: String? = null,
    val room: String? = null,
)

/** 餐饮响应 */
data class SiteMealResponse(
    val time: LocalDateTime?,
    val label: String?,
    val remark: String?,
)

/** 住宿响应 */
data class SiteHotelResponse(
    val type: String,
    val typeColor: String?,
    val name: String?,
    val unit: String?,
    val room: String?,
)

/** 考察点位响应 */
data class SiteInspectionPointResponse(
    val name: String,
    val intro: String?,
    val images: List<String>,
)

/** 服务信息响应 */
data class SiteServiceResponse(
    val staff: List<SiteStaffGroupResponse>,
    val weather: List<SiteWeatherResponse>,
    val notices: List<SiteNoticeResponse>,
)

/** 接待人员分组响应 */
data class SiteStaffGroupResponse(
    val type: String?,
    val members: List<SiteContactResponse>,
)

/** 天气响应 */
data class SiteWeatherResponse(
    val date: String?,
    val city: String?,
    val desc: String?,
    val description: String?,
    val temp: String?,
)

/** 公告响应 */
data class SiteNoticeResponse(
    val title: String?,
    val content: String?,
)

/** 城市概况响应 */
data class SiteOverviewResponse(
    val image: String?,
    val sections: List<SiteOverviewSectionResponse>,
)

/** 城市概况分段响应 */
data class SiteOverviewSectionResponse(
    val title: String?,
    val content: String?,
)
