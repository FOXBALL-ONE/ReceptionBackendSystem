package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.AttendanceGuidelinesItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 参会须知的 Excel 导入服务。
 *
 * 读取工作簿第 8 个 sheet（index=7，"参会须知"），按 [AttendanceGuidelinesItem] 列结构解析为内存项；
 * 该内容最终汇入活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.PromptService]
 * 的参会须知字段，而非独立实体。
 */
@Service
class AttendanceGuidelinesExcelService {
    /** 从文件路径读取参会须知 sheet。 */
    fun importAttendanceGuidelines(filePath: String): List<AttendanceGuidelinesItem> {
        return FileInputStream(filePath).use { inputStream ->
            importAttendanceGuidelines(inputStream)
        }
    }

    /** 从上传文件读取参会须知 sheet。 */
    fun importAttendanceGuidelines(file: MultipartFile): List<AttendanceGuidelinesItem> {
        return file.inputStream.use { inputStream ->
            importAttendanceGuidelines(inputStream)
        }
    }

    /**
     * 读取"参会须知" sheet：按 [AttendanceGuidelinesItem] 表头解析并过滤空行。
     * 空行判定见 [AttendanceGuidelinesItem.isBlankRow]（说明为空视为空行）。
     */
    fun importAttendanceGuidelines(inputStream: InputStream): List<AttendanceGuidelinesItem> {
        return EasyExcel.read(inputStream)
            .head(AttendanceGuidelinesItem::class.java)
            .sheet(7)
            .doReadSync<AttendanceGuidelinesItem>()
            .filterNot { it.isBlankRow() }
    }

    private fun AttendanceGuidelinesItem.isBlankRow(): Boolean =
        description.isNullOrBlank()
}
