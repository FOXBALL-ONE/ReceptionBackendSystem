package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class PersonnelItem(

    @field:ExcelProperty(value = ["序号"], index = 0)
    var id: Long? = null,

    @field:ExcelProperty(value = ["姓名"], index = 1)
    var name: String? = null,

    @field:ExcelProperty(value = ["单位"], index = 2)
    var unit: String? = null,

    @field:ExcelProperty(value = ["类别"], index = 3)
    var position: String? = null,

    @field:ExcelProperty(value = ["考察组"], index = 4)
    var inspectionTeam: String? = null,
)
