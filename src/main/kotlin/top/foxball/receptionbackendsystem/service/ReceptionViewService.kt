package top.foxball.receptionbackendsystem.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.config.ImageProperties
import top.foxball.receptionbackendsystem.datasource.jdbc.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 数字接待系统前端视图服务。
 * 将数据库实体转换为前端所需的数据格式。
 */
@Service
@Transactional(readOnly = true)
class ReceptionViewService(
    private val activitiesRepository: ActivitiesRepository,
    private val personRepository: PersonRepository,
    private val carRepository: CarRepository,
    private val promptServiceRepository: PromptServiceRepository,
    private val imageProperties: ImageProperties,
) {

    /**
     * 获取元数据配置。
     * 返回格式与 index.html 的 meta 初始化逻辑保持一致。
     */
    fun getMeta(): Map<String, Any?> {
        val activity = currentActivity()
        val promptService = promptServiceRepository.findAll().firstOrNull()

        return mapOf(
            "access" to (activity?.isOpen != false),
            "eventName" to (activity?.name ?: activity?.masterTitle ?: "数字接待系统"),
            "headerTitle" to (activity?.masterTitle ?: "数字接待系统"),
            "title" to (activity?.masterTitle ?: "数字接待系统"),
            "subtitle" to (activity?.subtitle ?: ""),
            "heroLabel" to activity?.bannerTags,
            "dateRange" to formatDateRange(activity?.startTime, activity?.endTime),
            "bannerImage" to imageUrl(activity?.bannerUrl),
            "heroImage" to imageUrl(activity?.bannerUrl),
            "showLoading" to (activity?.isAnimation != false),
            "showMeetingLocation" to true,
            "serviceMode" to if (promptService?.attendanceInstructionsMode == true) 1 else 0,
            "attendNoticeTitle" to (promptService?.attendanceInstructionsTitle ?: "参会须知"),
            "attendNotice" to (promptService?.attendanceInstructionsContent ?: ""),
            "icpNumber" to activity?.icp,
            "techSupport" to activity?.technicalSupport,
            "closedTitle" to "系统暂停访问",
            "closedMessage" to "当前页面已关闭，请联系工作人员。"
        )
    }

    /**
     * 获取日程安排数据。
     * 返回格式：{ days: [{ date, groups: [{name, mapImage, route, meetings}] }] }
     */
    fun getSchedule(): Map<String, Any?> {
        val activity = currentActivity()

        if (activity == null || activity.schedules.isEmpty()) {
            return mapOf("days" to emptyList<Map<String, Any?>>())
        }

        val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日")

        // 将日程按日期分组
        val dayMap = mutableMapOf<String, MutableList<Map<String, Any?>>>()

        activity.schedules.forEach { schedule ->
            schedule.inspectionTeamItem.forEach { team ->
                val firstEvent = team.eventArrangements.firstOrNull()
                val dateKey = firstEvent?.startTime?.format(dateFormatter) ?: "待定"

                val groups = dayMap.getOrPut(dateKey) { mutableListOf() }

                groups.add(
                    mapOf(
                        "name" to (team.name ?: "考察组"),
                        "mapImage" to imageUrl(team.routeUrl),
                        "route" to team.routeNode,
                        "meetings" to team.eventArrangements.map { event ->
                            mapOf(
                                "time" to (event.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""),
                                "content" to (event.content ?: ""),
                                "location" to (event.location ?: "")
                            )
                        }
                    )
                )
            }
        }

        val days = dayMap.map { (date, groups) ->
            mapOf(
                "date" to date,
                "groups" to groups
            )
        }

        return mapOf("days" to days)
    }

    /**
     * 获取人员信息列表。
     * 返回格式：[{ name, unit, type, typeColor, group, avatar }]
     */
    fun getPeople(): List<Map<String, Any?>> {
        val activity = currentActivity()
        val persons = activity?.id?.let(personRepository::findByActivityId) ?: emptyList()

        return persons.map { person ->
            mapOf(
                "name" to person.name,
                "unit" to person.unit,
                "type" to "参会人员",
                "typeColor" to "#2563eb", // 蓝色主题
                "group" to person.unit,
                "avatar" to null // 可以从图片库关联
            )
        }
    }

    /**
     * 获取车辆安排信息。
     * 返回格式：[{ id, carPlate, plate, driver: {name, phone}, passengers: [], leaders: [{name, phone}] }]
     */
    fun getVehicles(): List<Map<String, Any?>> {
        val activity = currentActivity()
        val cars = activity?.id?.let(carRepository::findByActivityId) ?: emptyList()

        return cars.map { car ->
            mapOf(
                "id" to car.carNumber,
                "carPlate" to car.carPlate,
                "plate" to car.carPlate,
                "driver" to mapOf(
                    "name" to car.driver,
                    "phone" to car.driverNumber
                ),
                "passengers" to car.passengersList.map { it.name },
                "leaders" to car.passengersOnBoardList.map { leader ->
                    mapOf(
                        "name" to leader.name,
                        "phone" to leader.phone
                    )
                }
            )
        }
    }

    /**
     * 获取用餐安排信息。
     * 返回格式：[{ time, label, remark }]
     */
    fun getMeals(): List<Map<String, Any?>> {
        val activity = currentActivity()

        return activity?.mealList?.map { meal ->
            mapOf(
                "time" to meal.time?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "label" to meal.name,
                "remark" to listOfNotNull(
                    meal.position?.takeIf { it.isNotBlank() }?.let { "地点：$it" },
                    meal.description?.takeIf { it.isNotBlank() }
                ).joinToString("\n")
            )
        } ?: emptyList()
    }

    /**
     * 获取住宿信息。
     * 返回格式：[{ name, unit, type, typeColor, room }]
     */
    fun getHotel(): List<Map<String, Any?>> {
        val activity = currentActivity()

        return activity?.hostedList?.mapNotNull { hosted ->
            hosted.person?.let { person ->
                mapOf(
                    "name" to person.name,
                    "unit" to person.unit,
                    "type" to (hosted.hostedColorsTag?.name ?: "参会人员"),
                    "typeColor" to (hosted.hostedColorsTag?.color ?: "#2563eb"),
                    "room" to hosted.roomNumber
                )
            }
        } ?: emptyList()
    }

    /**
     * 获取考察点信息。
     * 返回格式：[{ name, intro, images: [] }]
     */
    fun getSites(): List<Map<String, Any?>> {
        val activity = currentActivity()

        return activity?.inspectionPoints?.mapIndexed { index, point ->
            mapOf(
                "name" to "考察点${index + 1}",
                "intro" to point.description,
                "images" to listOfNotNull(imageUrl(point.imageURL))
            )
        } ?: emptyList()
    }

    /**
     * 获取服务信息。
     * 返回格式：{ staff: [{type, members: [{name, phone}]}], weather: [], notices: [] }
     */
    fun getService(): Map<String, Any?> {
        val promptService = promptServiceRepository.findAll().firstOrNull()
        val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日")

        return mapOf(
            "staff" to (promptService?.staffList?.map { staff ->
                mapOf(
                    "type" to (staff.name ?: staff.colorTag ?: "工作人员"),
                    "members" to staff.groupList.map { member ->
                        mapOf(
                            "name" to listOfNotNull(member.name, member.duty).joinToString(" "),
                            "phone" to member.phone
                        )
                    }
                )
            } ?: emptyList()),
            "weather" to (promptService?.weatherList?.map { weather ->
                mapOf(
                    "date" to (weather.time?.format(dateFormatter) ?: ""),
                    "city" to weather.city,
                    "desc" to weather.weatherDescriptor,
                    "description" to weather.weatherDescriptor,
                    "temp" to weather.temperature
                )
            } ?: emptyList()),
            "notices" to (promptService?.noteList?.map { note ->
                mapOf(
                    "title" to note.title,
                    "content" to note.content
                )
            } ?: emptyList())
        )
    }

    /**
     * 获取概况介绍。
     * 返回格式：{ image, sections: [{title, content}] }
     */
    fun getOverview(): Map<String, Any?> {
        val activity = currentActivity()

        val sections = activity?.overviewOfTheCityAndCounty?.map { overview ->
            mapOf(
                "title" to overview.title,
                "content" to overview.description.joinToString("\n\n") { paragraph ->
                    listOfNotNull(
                        paragraph.title?.takeIf { it.isNotBlank() },
                        paragraph.content?.takeIf { it.isNotBlank() }
                    ).joinToString("\n")
                }
            )
        } ?: emptyList()

        return mapOf(
            "image" to imageUrl(activity?.overviewOfTheCityAndCounty?.firstOrNull()?.topImageUrl),
            "sections" to sections
        )
    }

    /** 将展示接口中的图片字段转换为 index.html 可直接加载的 URL。 */
    private fun imageUrl(rawPath: String?): String? = imageProperties.publicUrl(rawPath)

    private fun currentActivity(): Activities? {
        return activitiesRepository.findByIsOpenTrueOrderByStartTimeAsc().firstOrNull()
            ?: activitiesRepository.findAll().minWithOrNull(compareBy<Activities> { it.startTime ?: LocalDateTime.MAX }.thenBy { it.id ?: Int.MAX_VALUE })
    }

    private fun formatDateRange(startTime: LocalDateTime?, endTime: LocalDateTime?): String? {
        val start = startTime?.toLocalDate()
        val end = endTime?.toLocalDate()
        return when {
            start == null && end == null -> null
            start != null && end == null -> start.format(FULL_DATE)
            start == null && end != null -> end.format(FULL_DATE)
            start == end -> start?.format(FULL_DATE)
            start?.year == end?.year -> "${start?.format(FULL_DATE)} - ${end?.format(SHORT_DATE)}"
            else -> "${start?.format(FULL_DATE)} - ${end?.format(FULL_DATE)}"
        }
    }

    private companion object {
        val FULL_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
        val SHORT_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日")
    }
}
