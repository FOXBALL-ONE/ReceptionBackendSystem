package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

data class ScheduleItem(
    var date: String? = null,

    var inspectionTeam: String? = null,

    var line: String? = null,

    var eventList: MutableList<TimeAndContextItem> = mutableListOf(),
)

data class TimeAndContextItem(
    var time: String? = null,

    var context: String? = null,

    var location: String? = null,
)

data class ScheduleExcelRow(
    @field:ExcelProperty(value = ["日期"], index = 0)
    var date: String? = null,

    @field:ExcelProperty(value = ["考察组"], index = 1)
    var inspectionTeam: String? = null,

    @field:ExcelProperty(value = ["路线"], index = 2)
    var line: String? = null,

    @field:ExcelProperty(value = ["时间"], index = 3)
    var time: String? = null,

    @field:ExcelProperty(value = ["内容"], index = 4)
    var context: String? = null,

    @field:ExcelProperty(value = ["地点"], index = 5)
    var location: String? = null,
)
