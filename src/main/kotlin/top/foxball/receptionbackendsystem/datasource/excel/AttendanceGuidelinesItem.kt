package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class AttendanceGuidelinesItem(

    @field:ExcelProperty(value = ["参会须知"], index = 0)
    var description: String? = null,
)
