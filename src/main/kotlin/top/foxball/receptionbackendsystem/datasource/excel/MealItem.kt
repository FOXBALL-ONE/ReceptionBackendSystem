package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty
import com.alibaba.excel.annotation.format.DateTimeFormat
import java.time.LocalDateTime

data class MealItem(

    @field:ExcelProperty(value = ["时间"], index = 0)
    @field:DateTimeFormat("yyyy-MM-dd HH:mm")
    var time: LocalDateTime? = null,

    @field:ExcelProperty(value = ["餐次"], index = 1)
    var mealTime: String? = null,

    @field:ExcelProperty(value = ["地点"], index = 2)
    var location: String? = null,

    @field:ExcelProperty(value = ["备注"], index = 3)
    var remark: String? = null,
)
