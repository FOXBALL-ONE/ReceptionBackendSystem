package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.InspectionPointsItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 考察点的 Excel 导入服务。
 *
 * 读取工作簿第 7 个 sheet（index=6，"考察点"），按 [InspectionPointsItem] 列结构解析为内存考察点项，
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint] 实体。
 */
@Service
class InspectionPointsExcelService {
    /** 从文件路径读取考察点 sheet。 */
    fun importInspectionPoints(filePath: String): List<InspectionPointsItem> {
        return FileInputStream(filePath).use { inputStream ->
            importInspectionPoints(inputStream)
        }
    }

    /** 从上传文件读取考察点 sheet。 */
    fun importInspectionPoints(file: MultipartFile): List<InspectionPointsItem> {
        return file.inputStream.use { inputStream ->
            importInspectionPoints(inputStream)
        }
    }

    /**
     * 读取"考察点" sheet：按 [InspectionPointsItem] 表头解析并过滤空行。
     * 空行判定见 [InspectionPointsItem.isBlankRow]（名称与说明均为空视为空行）。
     */
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
