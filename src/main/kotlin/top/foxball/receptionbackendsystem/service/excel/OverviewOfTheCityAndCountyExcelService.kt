package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class OverviewOfTheCityAndCountyExcelService {
    fun importOverviewOfTheCityAndCounty(filePath: String): List<OverviewOfTheCityAndCountyItem> {
        return FileInputStream(filePath).use { inputStream ->
            importOverviewOfTheCityAndCounty(inputStream)
        }
    }

    fun importOverviewOfTheCityAndCounty(file: MultipartFile): List<OverviewOfTheCityAndCountyItem> {
        return file.inputStream.use { inputStream ->
            importOverviewOfTheCityAndCounty(inputStream)
        }
    }

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
