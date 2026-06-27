package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 参会须知（考勤指引）的 Excel 行模型。
 *
 * 对应活动对外发布的参会须知文案，用于须知内容的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，仅有参会须知一列。
 */
data class AttendanceGuidelinesItem(

    /** 对应 Excel「参会须知」列：须知条目正文。 */
    @field:ExcelProperty(value = ["参会须知"], index = 0)
    var description: String? = null,
)
