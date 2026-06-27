package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleItem
import top.foxball.receptionbackendsystem.datasource.excel.TimeAndContextItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 日程安排的 Excel 导入服务。
 *
 * 读取工作簿第 6 个 sheet（index=5，"日程安排"），按 [ScheduleExcelRow] 列结构解析为扁平行，
 * 再按"日期/考察组/线路"作为表头聚合成 [ScheduleItem]（每个日程项含若干事件安排）。
 * 该结果对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.Schedule]
 * 与 [top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItinerary] 的输入。
 */
@Service
class ScheduleExcelService {
    /** 从文件路径读取日程安排 sheet。 */
    fun importSchedule(filePath: String): List<ScheduleItem> {
        return FileInputStream(filePath).use { inputStream ->
            importSchedule(inputStream)
        }
    }

    /** 从上传文件读取日程安排 sheet。 */
    fun importSchedule(file: MultipartFile): List<ScheduleItem> {
        return file.inputStream.use { inputStream ->
            importSchedule(inputStream)
        }
    }

    /**
     * 读取"日程安排" sheet 并按日程表头分组合并。
     * "日期/考察组/线路"任一非空视为新日程起始行；后续无表头行的"时间/内容/地点"挂到当前日程，
     * 三者全空的行视为空行跳过。
     */
    fun importSchedule(inputStream: InputStream): List<ScheduleItem> {
        val rows = EasyExcel.read(inputStream)
            .head(ScheduleExcelRow::class.java)
            .sheet(5)
            .doReadSync<ScheduleExcelRow>()

        return rows.toScheduleItems()
    }

    private fun List<ScheduleExcelRow>.toScheduleItems(): List<ScheduleItem> {
        val schedules = mutableListOf<ScheduleItem>()
        var currentSchedule: ScheduleItem? = null

        for (row in this) {
            if (row.isBlankRow()) {
                continue
            }

            val hasNewScheduleHeader = row.date.isNotBlankValue() ||
                row.inspectionTeam.isNotBlankValue() ||
                row.line.isNotBlankValue()

            if (hasNewScheduleHeader) {
                currentSchedule = ScheduleItem(
                    date = row.date?.trimToNull(),
                    inspectionTeam = row.inspectionTeam?.trimToNull(),
                    line = row.line?.trimToNull(),
                )
                schedules += currentSchedule
            }

            val schedule = currentSchedule ?: continue
            row.toEvent()?.let { schedule.eventList += it }
        }

        return schedules
    }

    private fun ScheduleExcelRow.toEvent(): TimeAndContextItem? {
        val time = time?.trimToNull()
        val context = context?.trimToNull()
        val location = location?.trimToNull()

        if (time == null && context == null && location == null) {
            return null
        }

        return TimeAndContextItem(
            time = time,
            context = context,
            location = location,
        )
    }

    private fun ScheduleExcelRow.isBlankRow(): Boolean =
        date.isNullOrBlank() &&
            inspectionTeam.isNullOrBlank() &&
            line.isNullOrBlank() &&
            time.isNullOrBlank() &&
            context.isNullOrBlank() &&
            location.isNullOrBlank()

    private fun String?.isNotBlankValue(): Boolean = !isNullOrBlank()

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }
}
