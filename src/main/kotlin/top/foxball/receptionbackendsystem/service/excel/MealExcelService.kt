package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.MealItem
import java.io.FileInputStream
import java.io.InputStream

/**
 * 用餐安排的 Excel 导入服务。
 *
 * 读取工作簿第 3 个 sheet（index=2，"用餐安排"），按 [MealItem] 列结构解析为内存中的用餐项，
 * 对应活动下属的 [top.foxball.receptionbackendsystem.datasource.jdbc.Meal] 实体。
 */
@Service
class MealExcelService {
    /** 从文件路径读取用餐安排 sheet。 */
    fun importMeal(filePath: String): List<MealItem> {
        return FileInputStream(filePath).use { inputStream ->
            importMeal(inputStream)
        }
    }

    /** 从上传文件读取用餐安排 sheet。 */
    fun importMeal(file: MultipartFile): List<MealItem> {
        return file.inputStream.use { inputStream ->
            importMeal(inputStream)
        }
    }

    /**
     * 读取"用餐安排" sheet：按 [MealItem] 表头解析并过滤空行。
     * 空行判定见 [MealItem.isBlankRow]（餐次与地点均为空视为空行）。
     */
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