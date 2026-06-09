package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class LodgingItem(

    @field:ExcelProperty(value = ["姓名"], index = 0)
    var name: String? = null,

    @field:ExcelProperty(value = ["单位"], index = 1)
    var unit: String? = null,

    @field:ExcelProperty(value = ["房号"], index = 2)
    var roomNumber: String? = null,

    @field:ExcelProperty(value = ["类别"], index = 3)
    var position: String? = null,
)
