package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.MealItem
import java.io.FileInputStream
import java.io.InputStream

@Service
class MealExcelService {
    fun importMeal(filePath: String): List<MealItem> {
        return FileInputStream(filePath).use { inputStream ->
            importMeal(inputStream)
        }
    }

    fun importMeal(file: MultipartFile): List<MealItem> {
        return file.inputStream.use { inputStream ->
            importMeal(inputStream)
        }
    }

    fun importMeal(inputStream: InputStream): List<MealItem> {
        return EasyExcel.read(inputStream)
            .head(MealItem::class.java)
            .sheet(2)
            .doReadSync<MealItem>()
            .filterNot { it.isBlankRow() }
    }

    private fun MealItem.isBlankRow(): Boolean =
        mealTime == null &&
                location.isNullOrBlank()
}