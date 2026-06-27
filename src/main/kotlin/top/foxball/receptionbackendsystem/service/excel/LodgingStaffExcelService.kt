package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaff
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaffExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.Staff
import java.io.FileInputStream
import java.io.InputStream

/**
 * 工作人员分组的 Excel 导入服务。
 *
 * 读取工作簿第 5 个 sheet（index=4，"工作人员"），表头占前 2 行（headRowNumber=2）；
 * 按 [LodgingStaffExcelRow] 列结构解析为扁平行后按"单位"分组为 [LodgingStaff]（每组含成员列表）。
 * 该结果最终汇入活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.PromptService]
 * 的 staffList 字段，而非独立实体。
 */
@Service
class LodgingStaffExcelService {
    /** 从文件路径读取工作人员 sheet。 */
    fun importLodgingStaff(filePath: String): List<LodgingStaff> {
        return FileInputStream(filePath).use { inputStream ->
            importLodgingStaff(inputStream)
        }
    }

    /** 从上传文件读取工作人员 sheet。 */
    fun importLodgingStaff(file: MultipartFile): List<LodgingStaff> {
        return file.inputStream.use { inputStream ->
            importLodgingStaff(inputStream)
        }
    }

    /**
     * 读取"工作人员" sheet（表头 2 行）并按单位分组合并。
     * 单位列非空视为新组起始行；后续无单位行的联系方式挂到当前组，
     * 联系方式中的手机号用 [PhoneRegex] 抽出，剩余部分作为姓名。
     */
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
