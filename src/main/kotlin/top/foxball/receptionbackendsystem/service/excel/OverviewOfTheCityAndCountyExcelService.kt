package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 市县概况的 Excel 导入服务。
 *
 * 读取工作簿第 9 个 sheet（index=8，"市县概况"），表头占前 1 行（headRowNumber=1）；
 * 该 sheet 为单列"值"，按"标题—内容"两两配对的方式组装成 [OverviewOfTheCityAndCountyItem]。
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty] 实体。
 */
@Service
class OverviewOfTheCityAndCountyExcelService {
    /** 从文件路径读取市县概况 sheet。 */
    fun importOverviewOfTheCityAndCounty(filePath: String): List<OverviewOfTheCityAndCountyItem> {
        return FileInputStream(filePath).use { inputStream ->
            importOverviewOfTheCityAndCounty(inputStream)
        }
    }

    /** 从上传文件读取市县概况 sheet。 */
    fun importOverviewOfTheCityAndCounty(file: MultipartFile): List<OverviewOfTheCityAndCountyItem> {
        return file.inputStream.use { inputStream ->
            importOverviewOfTheCityAndCounty(inputStream)
        }
    }

    /**
     * 读取"市县概况" sheet 并按"标题—内容"两两配对。
     * 连续两个非空值视为一组（标题、内容），中间遇到空行则丢弃当前未配对的标题。
     */
    fun importOverviewOfTheCityAndCounty(inputStream: InputStream): List<OverviewOfTheCityAndCountyItem> {
        val values = EasyExcel.read(inputStream)
            .head(OverviewOfTheCityAndCountyExcelRow::class.java)
            .sheet(8)
            .headRowNumber(1)
            .doReadSync<OverviewOfTheCityAndCountyExcelRow>()
            .map { it.value?.trimToNull() }

        return values.toOverviewItems()
    }

    private fun List<String?>.toOverviewItems(): List<OverviewOfTheCityAndCountyItem> {
        val items = mutableListOf<OverviewOfTheCityAndCountyItem>()
        var currentNode: String? = null

        for (value in this) {
            if (value == null) {
                currentNode = null
                continue
            }

            if (currentNode == null) {
                currentNode = value
            } else {
                items += OverviewOfTheCityAndCountyItem(
                    node = currentNode,
                    content = value,
                )
                currentNode = null
            }
        }

        currentNode?.let { node ->
            items += OverviewOfTheCityAndCountyItem(node = node)
        }

        return items
    }

    private fun String.trimToNull(): String? = trim().takeIf { it.isNotEmpty() }
}
