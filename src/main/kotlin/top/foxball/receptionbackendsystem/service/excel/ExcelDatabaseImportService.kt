package top.foxball.receptionbackendsystem.service.excel

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.AttendanceGuidelinesItem
import top.foxball.receptionbackendsystem.datasource.excel.CarItem
import top.foxball.receptionbackendsystem.datasource.excel.InspectionPointsItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaff
import top.foxball.receptionbackendsystem.datasource.excel.MealItem as ExcelMealItem
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyItem as ExcelOverviewItem
import top.foxball.receptionbackendsystem.datasource.excel.PersonnelItem
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleItem
import top.foxball.receptionbackendsystem.datasource.excel.TimeAndContextItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.OneStaff
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.ParagraphContentItem
import top.foxball.receptionbackendsystem.datasource.jdbc.PassengersOnBoardItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.StaffItem
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.util.UUID

@Service
class ExcelDatabaseImportService(
    private val activitiesRepository: ActivitiesRepository,
    private val personnelExcelService: PersonnelExcelService,
    private val carExcelService: CarExcelService,
    private val mealExcelService: MealExcelService,
    private val lodgingExcelService: LodgingExcelService,
    private val lodgingStaffExcelService: LodgingStaffExcelService,
    private val scheduleExcelService: ScheduleExcelService,
    private val inspectionPointsExcelService: InspectionPointsExcelService,
    private val attendanceGuidelinesExcelService: AttendanceGuidelinesExcelService,
    private val overviewExcelService: OverviewOfTheCityAndCountyExcelService,
) {
    @Transactional
    fun importExcel(filePath: String, options: ExcelDatabaseImportOptions = ExcelDatabaseImportOptions()): ExcelDatabaseImportResult {
        val parsed = ParsedExcelWorkbook(
            personnel = personnelExcelService.importPersonnel(filePath),
            cars = carExcelService.importCar(filePath),
            meals = mealExcelService.importMeal(filePath),
            lodgings = lodgingExcelService.importLodging(filePath),
            lodgingStaff = lodgingStaffExcelService.importLodgingStaff(filePath),
            schedules = scheduleExcelService.importSchedule(filePath),
            inspectionPoints = inspectionPointsExcelService.importInspectionPoints(filePath),
            attendanceGuidelines = attendanceGuidelinesExcelService.importAttendanceGuidelines(filePath),
            overviews = overviewExcelService.importOverviewOfTheCityAndCounty(filePath),
        )

        return saveParsedWorkbook(parsed, options)
    }

    @Transactional
    fun importExcel(file: MultipartFile, options: ExcelDatabaseImportOptions = ExcelDatabaseImportOptions()): ExcelDatabaseImportResult {
        val bytes = file.bytes
        val parsed = ParsedExcelWorkbook(
            personnel = bytes.parse(personnelExcelService::importPersonnel),
            cars = bytes.parse(carExcelService::importCar),
            meals = bytes.parse(mealExcelService::importMeal),
            lodgings = bytes.parse(lodgingExcelService::importLodging),
            lodgingStaff = bytes.parse(lodgingStaffExcelService::importLodgingStaff),
            schedules = bytes.parse(scheduleExcelService::importSchedule),
            inspectionPoints = bytes.parse(inspectionPointsExcelService::importInspectionPoints),
            attendanceGuidelines = bytes.parse(attendanceGuidelinesExcelService::importAttendanceGuidelines),
            overviews = bytes.parse(overviewExcelService::importOverviewOfTheCityAndCounty),
        )

        val resolvedOptions = if (options.name == null && !file.originalFilename.isNullOrBlank()) {
            options.copy(name = file.originalFilename!!.substringBeforeLast('.'))
        } else {
            options
        }

        return saveParsedWorkbook(parsed, resolvedOptions)
    }

    private fun saveParsedWorkbook(
        parsed: ParsedExcelWorkbook,
        options: ExcelDatabaseImportOptions,
    ): ExcelDatabaseImportResult {
        val url = options.url ?: "excel-${UUID.randomUUID()}"
        if (options.replaceExistingByUrl) {
            activitiesRepository.findByUrl(url)?.let {
                activitiesRepository.delete(it)
                activitiesRepository.flush()
            }
        }

        val startTime = options.startTime ?: parsed.inferStartTime()
        val activity = Activities(
            url = url,
            masterTitle = options.masterTitle ?: options.name ?: "数字接待系统导入活动",
            subtitle = options.subtitle,
            name = options.name ?: "Excel导入活动",
            startTime = startTime,
            endTime = options.endTime,
            bannerTags = options.bannerTags ?: "Excel导入",
            isAnimation = options.isAnimation,
            isTopNavigationBar = options.isTopNavigationBar,
            isOpen = options.isOpen,
        )

        val peopleByName = parsed.personnel
            .mapNotNull { row -> row.name?.trimToNull()?.let { it to row } }
            .toMap()
        val colorTagsByName = mutableMapOf<String, ColorTag>()

        activity.personList = parsed.personnel.mapNotNull { it.toPerson(activity) }.toMutableList()
        activity.carList = parsed.cars.map { it.toCar(activity, peopleByName) }.toMutableList()
        activity.mealList = parsed.meals.map { it.toMeal(activity) }.toMutableList()
        activity.hostedList = parsed.lodgings.mapNotNull { it.toLodging(activity, peopleByName, colorTagsByName) }.toMutableList()
        activity.colorTagList = colorTagsByName.values.toMutableList()
        activity.inspectionPoints = parsed.inspectionPoints.map { it.toInspectionPoint(activity) }.toMutableList()
        activity.overviewOfTheCityAndCounty = parsed.overviews.map { it.toOverview(activity) }.toMutableList()
        activity.schedules = parsed.schedules.toSchedules(activity, startTime?.year ?: Year.now().value).toMutableList()
        activity.promptServiceList = mutableListOf(parsed.toPromptService(activity))

        bindActivityChildren(activity)

        val saved = activitiesRepository.saveAndFlush(activity)
        return ExcelDatabaseImportResult(
            activityId = saved.id,
            url = saved.url,
            personnelCount = parsed.personnel.size,
            carCount = parsed.cars.size,
            mealCount = parsed.meals.size,
            lodgingCount = parsed.lodgings.size,
            lodgingStaffGroupCount = parsed.lodgingStaff.size,
            scheduleCount = parsed.schedules.size,
            inspectionPointCount = parsed.inspectionPoints.size,
            attendanceGuidelineCount = parsed.attendanceGuidelines.size,
            overviewCount = parsed.overviews.size,
        )
    }

    private fun bindActivityChildren(activity: Activities) {
        activity.personList.forEach { it.activity = activity }
        activity.carList.forEach { it.activity = activity }
        activity.mealList.forEach { it.activity = activity }
        activity.hostedList.forEach { it.activity = activity }
        activity.colorTagList.forEach { it.activity = activity }
        activity.inspectionPoints.forEach { it.activity = activity }
        activity.overviewOfTheCityAndCounty.forEach { it.activity = activity }
        activity.schedules.forEach { schedule ->
            schedule.activity = activity
            schedule.inspectionTeamItem.forEach { it.activity = activity }
        }
        activity.promptServiceList.forEach { it.activity = activity }
    }

    private fun ParsedExcelWorkbook.inferStartTime(): LocalDateTime? =
        meals.mapNotNull { it.time }.minOrNull()

    private fun PersonnelItem.toPerson(activity: Activities): Person? {
        val personName = name?.trimToNull() ?: return null
        return Person(
            activity = activity,
            name = personName,
            unit = unit?.trimToNull(),
        )
    }

    private fun CarItem.toCar(activity: Activities, peopleByName: Map<String, PersonnelItem>): Car =
        Car(
            activity = activity,
            carNumber = carNumber,
            carPlate = carPlate?.trimToNull(),
            driver = driver?.trimToNull(),
            driverNumber = driverNumber?.trimToNull(),
            passengersOnBoardList = staff.map {
                PassengersOnBoardItem(
                    name = it.name?.trimToNull(),
                    phone = it.phone?.trimToNull(),
                )
            }.toMutableList(),
            passengersList = passengers.map { passengerName ->
                peopleByName[passengerName]?.toPersonSnapshot() ?: Person(name = passengerName)
            }.toMutableList(),
        )

    private fun PersonnelItem.toPersonSnapshot(): Person =
        Person(
            name = name?.trimToNull(),
            unit = unit?.trimToNull(),
        )

    private fun ExcelMealItem.toMeal(activity: Activities): Meal =
        Meal(
            activity = activity,
            name = mealTime?.trimToNull(),
            description = remark?.trimToNull(),
            position = location?.trimToNull(),
            time = time,
        )

    private fun LodgingItem.toLodging(
        activity: Activities,
        peopleByName: Map<String, PersonnelItem>,
        colorTagsByName: MutableMap<String, ColorTag>,
    ): Lodging? {
        val personName = name?.trimToNull() ?: return null
        val person = peopleByName[personName]?.toPersonSnapshot() ?: Person(name = personName, unit = unit?.trimToNull())
        return Lodging(
            roomNumber = roomNumber?.cleanRoomNumber(),
            person = person,
            colorTag = position?.trimToNull()?.let { colorTagsByName.getOrCreate(activity, it) },
        )
    }

    private fun MutableMap<String, ColorTag>.getOrCreate(activity: Activities, name: String): ColorTag =
        getOrPut(name) {
            ColorTag(
                activity = activity,
                name = name,
                type = ColorTag.TYPE_LODGING,
            )
        }

    private fun InspectionPointsItem.toInspectionPoint(activity: Activities): InspectionPoint =
        InspectionPoint(
            activity = activity,
            description = listOfNotNull(name?.trimToNull(), description?.trimToNull()).joinToString("\n"),
        )

    private fun ExcelOverviewItem.toOverview(activity: Activities): OverviewOfTheCityAndCounty =
        OverviewOfTheCityAndCounty(
            activity = activity,
            title = node?.trimToNull(),
            description = mutableListOf(
                ParagraphContentItem(
                    title = node?.trimToNull(),
                    content = content?.trimToNull(),
                )
            ),
        )

    private fun List<ScheduleItem>.toSchedules(activity: Activities, year: Int): List<Schedule> =
        groupBy { it.date?.trimToNull() ?: "待定" }
            .map { (date, scheduleItems) ->
                Schedule(
                    activity = activity,
                    scheduleTags = date,
                    inspectionTeamItem = scheduleItems.map { it.toInspectionTeamItem(activity, year) }.toMutableList(),
                )
            }

    private fun ScheduleItem.toInspectionTeamItem(activity: Activities, year: Int): InspectionTeamItem =
        InspectionTeamItem(
            activity = activity,
            name = inspectionTeam?.trimToNull(),
            routeNode = line.toRouteNodes(),
            eventArrangements = eventList.mapNotNull { it.toEventArrangement(date, year) }.toMutableList(),
        )

    private fun TimeAndContextItem.toEventArrangement(dateText: String?, year: Int): EventArrangementsItem? {
        val start = parseDateTime(dateText, time, year)
        val contentValue = context?.trimToNull()
        val locationValue = location?.trimToNull()
        if (start == null && contentValue == null && locationValue == null) {
            return null
        }

        return EventArrangementsItem(
            startTime = start,
            content = contentValue,
            location = locationValue,
        )
    }

    private fun ParsedExcelWorkbook.toPromptService(activity: Activities): PromptService =
        PromptService(
            activity = activity,
            staffList = lodgingStaff.map { group ->
                StaffItem(
                    name = group.unit?.trimToNull(),
                    groupList = group.staffList.map { staff ->
                        OneStaff(
                            name = staff.name?.trimToNull(),
                            duty = staff.roomNumber?.cleanRoomNumber()?.let { "房号$it" },
                            phone = staff.phone?.trimToNull(),
                        )
                    }.toMutableList(),
                )
            }.toMutableList(),
            attendanceInstructionsMode = attendanceGuidelines.isNotEmpty(),
            attendanceInstructionsTitle = "参会须知",
            attendanceInstructionsContent = attendanceGuidelines
                .mapNotNull(AttendanceGuidelinesItem::description)
                .mapNotNull { it.trimToNull() }
                .joinToString("\n\n")
                .takeIf { it.isNotBlank() },
        )

    private fun String?.toRouteNodes(): MutableList<String> =
        this
            ?.split("→", "->", "—", "-", ">", "至")
            ?.mapNotNull { it.trimToNull() }
            ?.toMutableList()
            ?: mutableListOf()

    private fun parseDateTime(dateText: String?, timeText: String?, year: Int): LocalDateTime? {
        val dateMatch = MonthDayRegex.find(dateText ?: return null) ?: return null
        val timeMatch = TimeRegex.find(timeText ?: return null) ?: return null
        val date = LocalDate.of(
            year,
            dateMatch.groupValues[1].toInt(),
            dateMatch.groupValues[2].toInt(),
        )
        val time = LocalTime.of(
            timeMatch.groupValues[1].toInt(),
            timeMatch.groupValues[2].toInt(),
        )
        return LocalDateTime.of(date, time)
    }

    private fun String.cleanRoomNumber(): String? =
        trim()
            .removePrefix("房号")
            .trim()
            .takeIf { it.isNotEmpty() }

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }

    private fun <T> ByteArray.parse(parser: (ByteArrayInputStream) -> T): T =
        ByteArrayInputStream(this).use(parser)

    private companion object {
        val MonthDayRegex = Regex("(\\d{1,2})月(\\d{1,2})日")
        val TimeRegex = Regex("(\\d{1,2}):(\\d{2})")
    }
}

data class ExcelDatabaseImportOptions(
    val url: String? = null,
    val name: String? = null,
    val masterTitle: String? = null,
    val subtitle: String? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val bannerTags: String? = null,
    val isAnimation: Boolean? = true,
    val isTopNavigationBar: Boolean? = true,
    val isOpen: Boolean? = true,
    val replaceExistingByUrl: Boolean = false,
)

data class ExcelDatabaseImportResult(
    val activityId: Long?,
    val url: String?,
    val personnelCount: Int,
    val carCount: Int,
    val mealCount: Int,
    val lodgingCount: Int,
    val lodgingStaffGroupCount: Int,
    val scheduleCount: Int,
    val inspectionPointCount: Int,
    val attendanceGuidelineCount: Int,
    val overviewCount: Int,
)

private data class ParsedExcelWorkbook(
    val personnel: List<PersonnelItem>,
    val cars: List<CarItem>,
    val meals: List<ExcelMealItem>,
    val lodgings: List<LodgingItem>,
    val lodgingStaff: List<LodgingStaff>,
    val schedules: List<ScheduleItem>,
    val inspectionPoints: List<InspectionPointsItem>,
    val attendanceGuidelines: List<AttendanceGuidelinesItem>,
    val overviews: List<ExcelOverviewItem>,
)
