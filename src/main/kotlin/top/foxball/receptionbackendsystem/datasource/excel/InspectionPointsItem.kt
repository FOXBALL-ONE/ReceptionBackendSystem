package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class InspectionPointsItem(

    @field:ExcelProperty(value = ["名称"], index = 0)
    var name: String? = null,

    @field:ExcelProperty(value = ["简介"], index = 1)
    var description: String? = null,
)
