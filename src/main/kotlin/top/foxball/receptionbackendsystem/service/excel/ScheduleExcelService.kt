package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleItem
import top.foxball.receptionbackendsystem.datasource.excel.TimeAndContextItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class ScheduleExcelService {
    fun importSchedule(filePath: String): List<ScheduleItem> {
        return FileInputStream(filePath).use { inputStream ->
            importSchedule(inputStream)
        }
    }

    fun importSchedule(file: MultipartFile): List<ScheduleItem> {
        return file.inputStream.use { inputStream ->
            importSchedule(inputStream)
        }
    }

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
