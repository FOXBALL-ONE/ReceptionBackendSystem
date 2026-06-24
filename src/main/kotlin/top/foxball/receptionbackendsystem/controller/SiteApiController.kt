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

    @GetMapping("/api/site/{activityUrl}/schedule", "/api/schedule")
    fun schedule(
        @PathVariable(required = false) activityUrl: String?,
    ): SiteScheduleResponse {
        val activityId = resolveOpenActivity(activityUrl).requiredId()
        val schedules = scheduleService.findByActivityId(activityId)
        return SiteScheduleResponse(
            days = schedules.mapIndexed { index, schedule ->
                schedule.toSiteScheduleDay(index)
            },
        )
    }

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
                passengers = car.passengersList.mapNotNull { it.displayName() },
                leaders = car.passengersOnBoardList.map {
                    SiteContactResponse(
                        name = it.name,
                        phone = it.phone,
                    )
                },
            )
        }
    }

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
                                name = listOfNotNull(member.name, member.duty).joinToString(" "),
                                phone = member.phone,
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

    private fun resolveOpenActivity(activityUrl: String?): Activities {
        val activity = resolveActivity(activityUrl)
        if (activity.isOpen != true) {
            throw ForbiddenException("活动未开放")
        }
        return activity
    }

    private fun Activities.requiredId(): Long =
        id ?: throw ResourceNotFoundException("activity not found")

    private fun Schedule.toSiteScheduleDay(index: Int): SiteScheduleDayResponse {
        val groups = inspectionTeamItem
            .takeIf { it.isNotEmpty() }
            ?.mapIndexed { groupIndex, team -> team.toSiteScheduleGroup(groupIndex) }
            ?: emptyList()

        val meetings = groups.flatMap { it.meetings }
        return SiteScheduleDayResponse(
            date = scheduleDateLabel(index, meetings),
            groups = groups,
            meetings = meetings,
        )
    }

    private fun Schedule.scheduleDateLabel(
        index: Int,
        meetings: List<SiteMeetingResponse>,
    ): String {
        val firstMeetingTime = inspectionTeamItem
            .flatMap { it.eventArrangements }
            .mapNotNull { it.startTime }
            .minOrNull()

        if (firstMeetingTime != null) {
            return firstMeetingTime.format(DATE_FORMATTER)
        }

        return firstTag(scheduleTags) ?: meetings.firstOrNull()?.time ?: "Day ${index + 1}"
    }

    private fun InspectionTeamItem.toSiteScheduleGroup(index: Int): SiteScheduleGroupResponse =
        SiteScheduleGroupResponse(
            name = name ?: "Group ${index + 1}",
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

    private fun Meal.remark(): String? =
        listOfNotNull(position, description)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .takeIf { it.isNotBlank() }

    private fun Person.displayName(): String? =
        nickName?.takeIf { it.isNotBlank() } ?: name

    private fun InspectionPoint.siteName(index: Int): String =
        description
            ?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.isNotBlank() }
            ?.take(24)
            ?: "Site ${index + 1}"

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

    private companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd")
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

data class SiteScheduleResponse(
    val days: List<SiteScheduleDayResponse>,
)

data class SiteScheduleDayResponse(
    val date: String,
    val groups: List<SiteScheduleGroupResponse>,
    val meetings: List<SiteMeetingResponse> = emptyList(),
)

data class SiteScheduleGroupResponse(
    val name: String,
    val mapImage: String? = null,
    val route: List<String> = emptyList(),
    val meetings: List<SiteMeetingResponse> = emptyList(),
)

data class SiteMeetingResponse(
    val time: String,
    val content: String?,
    val location: String?,
)

data class SitePersonResponse(
    val type: String,
    val name: String?,
    val unit: String?,
    val group: String?,
    val avatar: String?,
    val typeColor: String?,
)

data class SiteVehicleResponse(
    val id: Long?,
    val plate: String?,
    val driver: SiteContactResponse?,
    val passengers: List<String>,
    val leaders: List<SiteContactResponse>,
)

data class SiteContactResponse(
    val name: String?,
    val phone: String?,
)

data class SiteMealResponse(
    val time: LocalDateTime?,
    val label: String?,
    val remark: String?,
)

data class SiteHotelResponse(
    val type: String,
    val typeColor: String?,
    val name: String?,
    val unit: String?,
    val room: String?,
)

data class SiteInspectionPointResponse(
    val name: String,
    val intro: String?,
    val images: List<String>,
)

data class SiteServiceResponse(
    val staff: List<SiteStaffGroupResponse>,
    val weather: List<SiteWeatherResponse>,
    val notices: List<SiteNoticeResponse>,
)

data class SiteStaffGroupResponse(
    val type: String?,
    val members: List<SiteContactResponse>,
)

data class SiteWeatherResponse(
    val date: String?,
    val city: String?,
    val desc: String?,
    val description: String?,
    val temp: String?,
)

data class SiteNoticeResponse(
    val title: String?,
    val content: String?,
)

data class SiteOverviewResponse(
    val image: String?,
    val sections: List<SiteOverviewSectionResponse>,
)

data class SiteOverviewSectionResponse(
    val title: String?,
    val content: String?,
)
