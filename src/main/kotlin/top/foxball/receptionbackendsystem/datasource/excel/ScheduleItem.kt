package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 日程分组的内存中间模型。
 *
 * 用于按日期聚合活动日程（Schedule 实体），同一日期、考察组、路线下的多个时段事件聚合到 [eventList] 中，
 * 供业务层聚合后再转换为 [ScheduleExcelRow] 进行 Excel 导入导出。
 */
data class ScheduleItem(
    /** 日程所属日期。 */
    var date: String? = null,

    /** 所属考察组名称。 */
    var inspectionTeam: String? = null,

    /** 该日考察路线说明。 */
    var line: String? = null,

    /** 该日各时段事件列表。 */
    var eventList: MutableList<TimeAndContextItem> = mutableListOf(),
)

/**
 * 日程中单个时段事件的内存中间模型。
 *
 * 表示某一时刻的事件内容与地点，作为 [ScheduleItem.eventList] 的元素。
 */
data class TimeAndContextItem(
    /** 事件时间。 */
    var time: String? = null,

    /** 事件内容说明。 */
    var context: String? = null,

    /** 事件地点。 */
    var location: String? = null,
)

/**
 * 活动日程的 Excel 行模型。
 *
 * 对应活动下属的日程记录（Schedule 实体 / activity 关联），由 [ScheduleItem] 展平得到，用于日程信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为日期、考察组、路线、时间、内容、地点。
 */
data class ScheduleExcelRow(
    /** 对应 Excel「日期」列：日程所属日期。 */
    @field:ExcelProperty(value = ["日期"], index = 0)
    var date: String? = null,

    /** 对应 Excel「考察组」列：所属考察组名称。 */
    @field:ExcelProperty(value = ["考察组"], index = 1)
    var inspectionTeam: String? = null,

    /** 对应 Excel「路线」列：当日考察路线说明。 */
    @field:ExcelProperty(value = ["路线"], index = 2)
    var line: String? = null,

    /** 对应 Excel「时间」列：事件发生时间。 */
    @field:ExcelProperty(value = ["时间"], index = 3)
    var time: String? = null,

    /** 对应 Excel「内容」列：事件内容说明。 */
    @field:ExcelProperty(value = ["内容"], index = 4)
    var context: String? = null,

    /** 对应 Excel「地点」列：事件地点。 */
    @field:ExcelProperty(value = ["地点"], index = 5)
    var location: String? = null,
)
