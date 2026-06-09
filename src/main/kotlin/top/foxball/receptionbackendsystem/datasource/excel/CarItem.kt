package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class CarItem(
    var carNumber: Long? = null,

    var carPlate: String? = null,

    var driver: String? = null,

    var driverNumber: String? = null,

    var staff: MutableList<StaffItem> = mutableListOf(),

    var passengers: MutableList<String> = mutableListOf(),
)

data class StaffItem(
    var name: String? = null,

    var phone: String? = null,
)

data class CarExcelRow(
    @field:ExcelProperty(value = ["车号"], index = 0)
    var carNumber: Long? = null,

    @field:ExcelProperty(value = ["车牌号"], index = 1)
    var carPlate: String? = null,

    @field:ExcelProperty(value = ["司机"], index = 2)
    var driver: String? = null,

    @field:ExcelProperty(value = ["司机电话"], index = 3)
    var driverNumber: String? = null,

    @field:ExcelProperty(value = ["工作人员"], index = 4)
    var staffName: String? = null,

    @field:ExcelProperty(value = ["工作人员电话"], index = 5)
    var staffPhone: String? = null,

    @field:ExcelProperty(value = ["乘车人员"], index = 6)
    var passengerName: String? = null,
)
