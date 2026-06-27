package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 车辆安排的内存中间模型。
 *
 * 表示一辆车的整体安排：车号、车牌、司机及联系方式，以及随车工作人员列表与乘车人员名单，
 * 供业务层聚合后再转换为 [CarExcelRow] 进行 Excel 导入导出。
 */
data class CarItem(
    /** 车辆序号（车号）。 */
    var carNumber: Long? = null,

    /** 车牌号。 */
    var carPlate: String? = null,

    /** 司机姓名。 */
    var driver: String? = null,

    /** 司机联系电话。 */
    var driverNumber: String? = null,

    /** 随车工作人员列表。 */
    var staff: MutableList<StaffItem> = mutableListOf(),

    /** 乘车人员姓名列表。 */
    var passengers: MutableList<String> = mutableListOf(),
)

/**
 * 随车工作人员的内存中间模型。
 *
 * 表示单个随车工作人员的姓名与电话，作为 [CarItem.staff] 的元素。
 */
data class StaffItem(
    /** 随车工作人员姓名。 */
    var name: String? = null,

    /** 随车工作人员电话。 */
    var phone: String? = null,
)

/**
 * 车辆安排的 Excel 行模型。
 *
 * 对应活动下属的车辆记录（Car 实体 / activity 关联），由 [CarItem] 展平得到，用于车辆信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为车号、车牌号、司机、司机电话、工作人员、工作人员电话、乘车人员。
 */
data class CarExcelRow(
    /** 对应 Excel「车号」列：车辆序号。 */
    @field:ExcelProperty(value = ["车号"], index = 0)
    var carNumber: Long? = null,

    /** 对应 Excel「车牌号」列：车辆牌照号。 */
    @field:ExcelProperty(value = ["车牌号"], index = 1)
    var carPlate: String? = null,

    /** 对应 Excel「司机」列：司机姓名。 */
    @field:ExcelProperty(value = ["司机"], index = 2)
    var driver: String? = null,

    /** 对应 Excel「司机电话」列：司机联系电话。 */
    @field:ExcelProperty(value = ["司机电话"], index = 3)
    var driverNumber: String? = null,

    /** 对应 Excel「工作人员」列：随车工作人员姓名。 */
    @field:ExcelProperty(value = ["工作人员"], index = 4)
    var staffName: String? = null,

    /** 对应 Excel「工作人员电话」列：随车工作人员电话。 */
    @field:ExcelProperty(value = ["工作人员电话"], index = 5)
    var staffPhone: String? = null,

    /** 对应 Excel「乘车人员」列：乘车人员姓名。 */
    @field:ExcelProperty(value = ["乘车人员"], index = 6)
    var passengerName: String? = null,
)
