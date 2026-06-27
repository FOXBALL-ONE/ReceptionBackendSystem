package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty
import com.alibaba.excel.annotation.format.DateTimeFormat
import java.time.LocalDateTime

/**
 * 用餐安排的 Excel 行模型。
 *
 * 对应活动下属的用餐记录（Meal 实体 / activity 关联），用于用餐信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为时间、餐次、地点、备注。
 */
data class MealItem(

    /** 对应 Excel「时间」列：用餐时间，按 `yyyy-MM-dd HH:mm` 解析。 */
    @field:ExcelProperty(value = ["时间"], index = 0)
    @field:DateTimeFormat("yyyy-MM-dd HH:mm")
    var time: LocalDateTime? = null,

    /** 对应 Excel「餐次」列：用餐类别（如早/中/晚餐）。 */
    @field:ExcelProperty(value = ["餐次"], index = 1)
    var mealTime: String? = null,

    /** 对应 Excel「地点」列：用餐地点。 */
    @field:ExcelProperty(value = ["地点"], index = 2)
    var location: String? = null,

    /** 对应 Excel「备注」列：用餐备注说明。 */
    @field:ExcelProperty(value = ["备注"], index = 3)
    var remark: String? = null,
)
