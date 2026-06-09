package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.InspectionPointsItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class InspectionPointsExcelService {
    fun importInspectionPoints(filePath: String): List<InspectionPointsItem> {
        return FileInputStream(filePath).use { inputStream ->
            importInspectionPoints(inputStream)
        }
    }

    fun importInspectionPoints(file: MultipartFile): List<InspectionPointsItem> {
        return file.inputStream.use { inputStream ->
            importInspectionPoints(inputStream)
        }
    }

    fun importInspectionPoints(inputStream: InputStream): List<InspectionPointsItem> {
        return EasyExcel.read(inputStream)
            .head(InspectionPointsItem::class.java)
            .sheet(6)
            .doReadSync<InspectionPointsItem>()
            .filterNot { it.isBlankRow() }
    }

    private fun InspectionPointsItem.isBlankRow(): Boolean =
        name.isNullOrBlank() &&
            description.isNullOrBlank()
}
