package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 工作人员住宿分组的内存中间模型。
 *
 * 按单位聚合随车/服务工作人员住宿信息，同一单位下的多名工作人员聚合到 [staffList] 中，
 * 供业务层聚合后再转换为 [LodgingStaffExcelRow] 进行 Excel 导入导出。
 */
data class LodgingStaff(

    /** 工作人员所属单位。 */
    var unit: String? = null,

    /** 该单位下的工作人员列表。 */
    var staffList: MutableList<Staff> = mutableListOf(),

)

/**
 * 工作人员联系信息的内存中间模型。
 *
 * 表示单个工作人员的姓名、电话与房号，作为 [LodgingStaff.staffList] 的元素。
 */
data class Staff(
    /** 工作人员姓名。 */
    var name: String? = null,

    /** 工作人员联系电话。 */
    var phone: String? = null,

    /** 工作人员分配的房号。 */
    var roomNumber: String? = null,
)

/**
 * 工作人员联系方式的 Excel 行模型。
 *
 * 对应活动下属工作人员的住宿/联系方式记录，由 [LodgingStaff] 展平得到，用于工作人员信息的 Excel 导入导出；
 * 表头为二级合并表头（「工作人员联系方式」下的「单位」「联系人、电话」「房号」），列顺序由 `@ExcelProperty(index = ...)` 决定。
 */
data class LodgingStaffExcelRow(
    /** 对应 Excel「工作人员联系方式 / 单位」列：工作人员所属单位。 */
    @field:ExcelProperty(value = ["工作人员联系方式", "单位"], index = 0)
    var unit: String? = null,

    /** 对应 Excel「工作人员联系方式 / 联系人、电话」列：联系人姓名与电话。 */
    @field:ExcelProperty(value = ["工作人员联系方式", "联系人、电话"], index = 1)
    var contact: String? = null,

    /** 对应 Excel「工作人员联系方式 / 房号」列：工作人员分配的房号。 */
    @field:ExcelProperty(value = ["工作人员联系方式", "房号"], index = 2)
    var roomNumber: String? = null,
)
