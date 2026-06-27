package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.LodgingItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 住宿安排的 Excel 导入服务。
 *
 * 读取工作簿第 4 个 sheet（index=3，"住宿安排"），按 [LodgingItem] 列结构解析为内存住宿项，
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.Lodging] 实体。
 */
@Service
class LodgingExcelService {
    /** 从文件路径读取住宿安排 sheet。 */
    fun importLodging(filePath: String): List<LodgingItem> {
        return FileInputStream(filePath).use { inputStream ->
            importLodging(inputStream)
        }
    }

    /** 从上传文件读取住宿安排 sheet。 */
    fun importLodging(file: MultipartFile): List<LodgingItem> {
        return file.inputStream.use { inputStream ->
            importLodging(inputStream)
        }
    }

    /**
     * 读取"住宿安排" sheet：按 [LodgingItem] 表头解析并过滤空行。
     * 空行判定见 [LodgingItem.isBlankRow]（姓名/单位/房号/类别全空视为空行）。
     */
    fun importLodging(inputStream: InputStream): List<LodgingItem> {
        return EasyExcel.read(inputStream)
            .head(LodgingItem::class.java)
            .sheet(3)
            .doReadSync<LodgingItem>()
            .filterNot { it.isBlankRow() }
    }

    private fun LodgingItem.isBlankRow(): Boolean =
        name.isNullOrBlank() &&
            unit.isNullOrBlank() &&
            roomNumber.isNullOrBlank() &&
            position.isNullOrBlank()
}
