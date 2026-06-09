package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaff
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaffExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.Staff
import java.io.FileInputStream
import java.io.InputStream

@Service
class LodgingStaffExcelService {
    fun importLodgingStaff(filePath: String): List<LodgingStaff> {
        return FileInputStream(filePath).use { inputStream ->
            importLodgingStaff(inputStream)
        }
    }

    fun importLodgingStaff(file: MultipartFile): List<LodgingStaff> {
        return file.inputStream.use { inputStream ->
            importLodgingStaff(inputStream)
        }
    }

    fun importLodgingStaff(inputStream: InputStream): List<LodgingStaff> {
        val rows = EasyExcel.read(inputStream)
            .head(LodgingStaffExcelRow::class.java)
            .sheet(4)
            .headRowNumber(2)
            .doReadSync<LodgingStaffExcelRow>()

        return rows.toLodgingStaff()
    }

    private fun List<LodgingStaffExcelRow>.toLodgingStaff(): List<LodgingStaff> {
        val groups = mutableListOf<LodgingStaff>()
        var currentGroup: LodgingStaff? = null

        for (row in this) {
            if (row.isBlankRow()) {
                continue
            }

            row.unit?.trimToNull()?.let { unit ->
                currentGroup = LodgingStaff(unit = unit)
                groups += currentGroup
            }

            val group = currentGroup ?: continue
            row.toStaff()?.let { staff ->
                group.staffList += staff
            }
        }

        return groups
    }

    private fun LodgingStaffExcelRow.toStaff(): Staff? {
        val contact = contact?.trimToNull() ?: return null
        val phoneMatch = PhoneRegex.find(contact)
        val name = if (phoneMatch == null) {
            contact.cleanStaffName()
        } else {
            contact.substring(0, phoneMatch.range.first).cleanStaffName()
        }

        return Staff(
            name = name,
            phone = phoneMatch?.value,
            roomNumber = roomNumber?.trimToNull(),
        )
    }

    private fun LodgingStaffExcelRow.isBlankRow(): Boolean =
        unit.isNullOrBlank() &&
            contact.isNullOrBlank() &&
            roomNumber.isNullOrBlank()

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }

    private fun String.cleanStaffName(): String? =
        replace("\u3000", "")
            .replace(Regex("\\s+"), "")
            .trimToNull()

    private companion object {
        val PhoneRegex = Regex("1\\d{10}")
    }
}
