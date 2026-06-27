package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.ExcelWriter
import com.alibaba.excel.write.metadata.WriteSheet
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.excel.AttendanceGuidelinesItem
import top.foxball.receptionbackendsystem.datasource.excel.CarExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.InspectionPointsItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaffExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.MealItem as ExcelMealItem
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyItem
import top.foxball.receptionbackendsystem.datasource.excel.PersonnelItem
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleExcelRow
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItinerary
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import java.io.OutputStream
import java.time.format.DateTimeFormatter
import kotlin.io.use

/**
 * 整库 Excel 导出服务。
 *
 * 把一个活动下的全部业务数据（人员/车辆/用餐/住宿/工作人员/日程/考察点/参会须知/市县概况）
 * 按约定顺序写入 9 个 sheet，结构由各 sheet 的 head 类（XxxItem / XxxExcelRow）决定，
 * 与 [ExcelDatabaseImportService] 写出的模板一一对应。
 */
@Service
class ExcelDatabaseExportService(
    private val activitiesRepository: ActivitiesRepository,
) {
    /** 按活动主键导出整库为 Excel，写入 [outputStream]。 */
    @Transactional(readOnly = true)
    fun exportActivity(activityId: Long, outputStream: OutputStream) {
        val activity = activitiesRepository.findById(activityId)
            .orElseThrow { ResourceNotFoundException("活动不存在：$activityId") }

        writeWorkbook(activity, outputStream)
    }

    /** 按活动 url 导出整库为 Excel，写入 [outputStream]。 */
    @Transactional(readOnly = true)
    fun exportActivity(url: String, outputStream: OutputStream) {
        val activity = activitiesRepository.findByUrl(url)
            ?: throw ResourceNotFoundException("活动不存在：$url")

        writeWorkbook(activity, outputStream)
    }

    /** 把活动各子集合按 head 类转换为行并依次写入 9 个 sheet。 */
    private fun writeWorkbook(activity: Activities, outputStream: OutputStream) {
        EasyExcel.write(outputStream).build().use { writer ->
            writer.write(activity.toPersonnelRows(), sheet(0, "人员名单", PersonnelItem::class.java))
            writer.write(activity.toCarRows(), sheet(1, "乘车安排", CarExcelRow::class.java))
            writer.write(activity.toMealRows(), sheet(2, "用餐安排", ExcelMealItem::class.java))
            writer.write(activity.toLodgingRows(), sheet(3, "住宿安排", LodgingItem::class.java))
            writer.write(activity.toLodgingStaffRows(), sheet(4, "工作人员", LodgingStaffExcelRow::class.java))
            writer.write(activity.toScheduleRows(), sheet(5, "日程安排", ScheduleExcelRow::class.java))
            writer.write(activity.toInspectionPointRows(), sheet(6, "考察点", InspectionPointsItem::class.java))
            writer.write(activity.toAttendanceGuidelineRows(), sheet(7, "参会须知", AttendanceGuidelinesItem::class.java))
            writer.write(activity.toOverviewRows(), sheet(8, "市县概况", OverviewOfTheCityAndCountyExcelRow::class.java))
        }
    }

    /**
     * 生成空白导入模板：仅写出 9 个 sheet 的表头。
     *
     * head 类与 [top.foxball.receptionbackendsystem.service.excel.ExcelDatabaseImportService] 各
     * per-sheet 解析器读取的 head 类保持一致（注意「市县概况」使用导入侧的
     * [OverviewOfTheCityAndCountyItem]，与导出侧的 ExcelRow 列结构不同），从而保证用户按模板填写后可被完整解析。
     */
    fun writeTemplate(outputStream: OutputStream) {
        EasyExcel.write(outputStream).build().use { writer ->
            writer.write(emptyList<PersonnelItem>(), sheet(0, "人员名单", PersonnelItem::class.java))
            writer.write(emptyList<CarExcelRow>(), sheet(1, "乘车安排", CarExcelRow::class.java))
            writer.write(emptyList<ExcelMealItem>(), sheet(2, "用餐安排", ExcelMealItem::class.java))
            writer.write(emptyList<LodgingItem>(), sheet(3, "住宿安排", LodgingItem::class.java))
            writer.write(emptyList<LodgingStaffExcelRow>(), sheet(4, "工作人员", LodgingStaffExcelRow::class.java))
            writer.write(emptyList<ScheduleExcelRow>(), sheet(5, "日程安排", ScheduleExcelRow::class.java))
            writer.write(emptyList<InspectionPointsItem>(), sheet(6, "考察点", InspectionPointsItem::class.java))
            writer.write(emptyList<AttendanceGuidelinesItem>(), sheet(7, "参会须知", AttendanceGuidelinesItem::class.java))
            writer.write(emptyList<OverviewOfTheCityAndCountyItem>(), sheet(8, "市县概况", OverviewOfTheCityAndCountyItem::class.java))
        }
    }

    private fun sheet(index: Int, name: String, head: Class<*>): WriteSheet =
        EasyExcel.writerSheet(index, name).head(head).build()

    private fun Activities.toPersonnelRows(): List<PersonnelItem> {
        val teamNamesById = inspectionTeamItemList
            .mapNotNull { team -> team.id?.let { it to team.name } }
            .toMap()

        return personList.mapIndexed { index, person ->
            PersonnelItem(
                id = (index + 1).toLong(),
                name = person.name,
                unit = person.unit,
                position = null,
                inspectionTeam = person.inspectionTeamItemId?.let(teamNamesById::get),
            )
        }
    }

    private fun Activities.toCarRows(): List<CarExcelRow> =
        carList.flatMap { car -> car.toCarRows() }

    private fun Car.toCarRows(): List<CarExcelRow> {
        val passengerNames = passengersList.mapNotNull { it.name?.trimToNull() }.ifEmpty { listOf<String?>(null) }
        return passengerNames.mapIndexed { index, passengerName ->
            val staff = passengersOnBoardList.getOrNull(index)
            CarExcelRow(
                carNumber = if (index == 0) carNumber else null,
                carPlate = if (index == 0) carPlate else null,
                driver = if (index == 0) driver else null,
                driverNumber = if (index == 0) driverNumber else null,
                staffName = staff?.name,
                staffPhone = staff?.phone,
                passengerName = passengerName,
            )
        }
    }

    private fun Activities.toMealRows(): List<ExcelMealItem> =
        mealList.map { meal ->
            ExcelMealItem(
                time = meal.time,
                mealTime = meal.name,
                location = meal.position,
                remark = meal.description,
            )
        }

    private fun Activities.toLodgingRows(): List<LodgingItem> =
        hostedList.mapNotNull { it.toLodgingRow() }

    private fun Lodging.toLodgingRow(): LodgingItem? {
        val guest = person ?: return null
        return LodgingItem(
            name = guest.name,
            unit = guest.unit,
            roomNumber = roomNumber?.let { "房号$it" },
            position = colorTag?.name,
        )
    }

    private fun Activities.toLodgingStaffRows(): List<LodgingStaffExcelRow> =
        promptServiceList.firstOrNull()?.toLodgingStaffRows() ?: emptyList()

    private fun PromptService.toLodgingStaffRows(): List<LodgingStaffExcelRow> =
        staffList.flatMap { group ->
            val members = group.groupList.ifEmpty { mutableListOf(null) }
            members.mapIndexed { index, member ->
                LodgingStaffExcelRow(
                    unit = if (index == 0) group.name else null,
                    contact = member?.let {
                        listOfNotNull(it.name, it.phone).joinToString("")
                    },
                    roomNumber = member?.duty?.removePrefix("房号"),
                )
            }
        }

    private fun Activities.toScheduleRows(): List<ScheduleExcelRow> =
        inspectionTeamItemList.flatMap { team -> team.toScheduleRows() }

    private fun InspectionTeamItem.toScheduleRows(): List<ScheduleExcelRow> =
        itineraries.flatMap { itinerary -> itinerary.toScheduleRows(name) }

    private fun InspectionTeamItinerary.toScheduleRows(teamName: String?): List<ScheduleExcelRow> {
        val date = eventArrangements.firstOrNull()?.startTime?.format(DateFormatter)
            ?: schedule?.scheduleTags
        val route = routeNode.joinToString(" → ").takeIf { it.isNotBlank() }
        val events = eventArrangements.ifEmpty { mutableListOf(null) }

        return events.mapIndexed { index, event ->
            ScheduleExcelRow(
                date = if (index == 0) date else null,
                inspectionTeam = if (index == 0) teamName else null,
                line = if (index == 0) route else null,
                time = event?.startTime?.format(TimeFormatter),
                context = event?.content,
                location = event?.location,
            )
        }
    }

    private fun Activities.toInspectionPointRows(): List<InspectionPointsItem> =
        inspectionPoints.mapIndexed { index, point ->
            val parts = point.description?.split("\n", limit = 2) ?: emptyList()
            InspectionPointsItem(
                name = parts.getOrNull(0) ?: "考察点${index + 1}",
                description = parts.getOrNull(1) ?: point.description,
            )
        }

    private fun Activities.toAttendanceGuidelineRows(): List<AttendanceGuidelinesItem> =
        promptServiceList
            .mapNotNull { it.attendanceInstructionsContent?.trimToNull() }
            .map { AttendanceGuidelinesItem(description = it) }

    private fun Activities.toOverviewRows(): List<OverviewOfTheCityAndCountyExcelRow> =
        overviewOfTheCityAndCounty.flatMap { overview ->
            listOf(
                OverviewOfTheCityAndCountyExcelRow(value = overview.title),
                OverviewOfTheCityAndCountyExcelRow(
                    value = overview.description.joinToString("\n") { paragraph ->
                        listOfNotNull(paragraph.title, paragraph.content).joinToString("\n")
                    }.trimToNull()
                ),
            )
        }

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }

    private companion object {
        val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日")
        val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
