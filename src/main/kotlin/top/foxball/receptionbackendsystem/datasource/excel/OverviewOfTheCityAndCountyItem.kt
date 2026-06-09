package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class OverviewOfTheCityAndCountyItem(

    @field:ExcelProperty(value = ["市县地区"], index = 0)
    var node: String? = null,

    @field:ExcelProperty(value = ["基本情况"], index = 1)
    var content: String? = null,
)

data class OverviewOfTheCityAndCountyExcelRow(
    @field:ExcelProperty(index = 0)
    var value: String? = null,
)
