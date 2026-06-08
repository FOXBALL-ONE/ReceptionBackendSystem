package top.foxball.receptionbackendsystem.service

import org.springframework.stereotype.Service
import top.foxball.receptionbackendsystem.datasource.jdbc.*
import java.time.format.DateTimeFormatter

/**
 * 数字接待系统前端视图服务。
 * 将数据库实体转换为前端所需的数据格式。
 */
@Service
class ReceptionViewService(
    private val activitiesRepository: ActivitiesRepository,
    private val scheduleRepository: ScheduleRepository,
    private val personRepository: PersonRepository,
    private val carRepository: CarRepository
) {

    /**
     * 获取元数据配置。
     * 返回格式：{ title, subtitle, logo, heroImage, showLoading }
     */
    fun getMeta(): Map<String, Any?> {
        // 获取当前活动（这里简化处理，取第一个活动）
        val activity = activitiesRepository.findAll().firstOrNull()

        return mapOf(
            "title" to (activity?.masterTitle ?: "数字接待系统"),
            "subtitle" to (activity?.subtitle ?: ""),
            "logo" to null, // 可以从配置或图片库获取
            "heroImage" to activity?.bannerUrl, // 从活动获取横幅图
            "showLoading" to true
        )
    }

    /**
     * 获取日程安排数据。
     * 返回格式：{ days: [{ date, groups: [{name, mapImage, route, meetings}] }] }
     */
    fun getSchedule(): Map<String, Any?> {
        // 从活动中获取关联的日程和考察组
        val activity = activitiesRepository.findAll().firstOrNull()

        if (activity == null || activity.schedules.isEmpty()) {
            return mapOf("days" to emptyList<Map<String, Any?>>())
        }

        val dateFormatter = DateTimeFormatter.ofPattern("MM月dd日")

        // 将日程按日期分组
        val dayMap = mutableMapOf<String, MutableList<Map<String, Any?>>>()

        activity.schedules.forEach { schedule ->
            schedule.inspectionTeamItem.forEach { team ->
                // 从事件安排中提取日期
                val firstEvent = team.eventArrangements.firstOrNull()
                val dateKey = firstEvent?.startTime?.format(dateFormatter) ?: "待定"

                val groups = dayMap.getOrPut(dateKey) { mutableListOf() }

                // 构建考察组数据
                groups.add(
                    mapOf(
                        "name" to (team.name ?: "考察组"),
                        "mapImage" to team.routeUrl, // 路线图
                        "route" to team.routeNode, // 路线节点
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

        // 构建最终的days数据
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
        val persons = personRepository.findAll()

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
     * 返回格式：[{ id, plate, driver: {name, phone}, passengers: [], leaders: [{name, phone}] }]
     */
    fun getVehicles(): List<Map<String, Any?>> {
        val cars = carRepository.findAll()

        return cars.map { car ->
            mapOf(
                "id" to car.carNumber,
                "plate" to car.carNumber?.toString(),
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
        val activity = activitiesRepository.findAll().firstOrNull()

        return activity?.mealList?.map { meal ->
            mapOf(
                "time" to meal.time,
                "label" to meal.name,
                "remark" to meal.description
            )
        } ?: emptyList()
    }

    /**
     * 获取住宿信息。
     * 返回格式：[{ name, unit, type, typeColor, room }]
     */
    fun getHotel(): List<Map<String, Any?>> {
        val activity = activitiesRepository.findAll().firstOrNull()

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
        val activity = activitiesRepository.findAll().firstOrNull()

        return activity?.inspectionPoints?.map { point ->
            mapOf(
                "name" to "考察点",
                "intro" to point.description,
                "images" to listOfNotNull(point.imageURL)
            )
        } ?: emptyList()
    }

    /**
     * 获取服务信息。
     * 返回格式：{ staff: [{type, members: [{name, phone}]}], weather: [], notices: [] }
     */
    fun getService(): Map<String, Any?> {
        // 这里可以配置工作人员信息
        val staff = listOf(
            mapOf(
                "type" to "联系人",
                "members" to listOf(
                    mapOf("name" to "客服", "phone" to "")
                )
            )
        )

        return mapOf(
            "staff" to staff,
            "weather" to emptyList<Map<String, Any?>>(),
            "notices" to emptyList<Map<String, Any?>>()
        )
    }

    /**
     * 获取概况介绍。
     * 返回格式：{ image, sections: [{title, content}] }
     */
    fun getOverview(): Map<String, Any?> {
        val activity = activitiesRepository.findAll().firstOrNull()

        val sections = activity?.overviewOfTheCityAndCounty?.map { overview ->
            mapOf(
                "title" to overview.title,
                "content" to overview.description.joinToString("\n\n") { paragraph ->
                    "${paragraph.title ?: ""}\n${paragraph.content ?: ""}"
                }
            )
        } ?: emptyList()

        return mapOf(
            "image" to activity?.overviewOfTheCityAndCounty?.firstOrNull()?.topImageUrl,
            "sections" to sections
        )
    }
}
