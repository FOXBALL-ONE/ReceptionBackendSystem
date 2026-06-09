package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class LodgingStaff(

    var unit: String? = null,
    var staffList: MutableList<Staff> = mutableListOf(),

)

data class Staff(
    var name: String? = null,
    var phone: String? = null,
    var roomNumber: String? = null,
)

data class LodgingStaffExcelRow(
    @field:ExcelProperty(value = ["单位"], index = 0)
    var unit: String? = null,

    @field:ExcelProperty(value = ["联系人、电话"], index = 1)
    var contact: String? = null,

    @field:ExcelProperty(value = ["房号"], index = 2)
    var roomNumber: String? = null,
)
