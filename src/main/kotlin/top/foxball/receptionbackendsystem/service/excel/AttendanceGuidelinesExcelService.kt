package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.AttendanceGuidelinesItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class AttendanceGuidelinesExcelService {
    fun importAttendanceGuidelines(filePath: String): List<AttendanceGuidelinesItem> {
        return FileInputStream(filePath).use { inputStream ->
            importAttendanceGuidelines(inputStream)
        }
    }

    fun importAttendanceGuidelines(file: MultipartFile): List<AttendanceGuidelinesItem> {
        return file.inputStream.use { inputStream ->
            importAttendanceGuidelines(inputStream)
        }
    }

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
