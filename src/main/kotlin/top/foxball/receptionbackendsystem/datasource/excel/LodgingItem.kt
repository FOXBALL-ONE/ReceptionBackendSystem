package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 住宿安排的 Excel 行模型。
 *
 * 对应活动下属的住宿记录（Lodging 实体 / activity 关联），用于住宿信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为姓名、单位、房号、类别。
 */
data class LodgingItem(

    /** 对应 Excel「姓名」列：住宿人员姓名。 */
    @field:ExcelProperty(value = ["姓名"], index = 0)
    var name: String? = null,

    /** 对应 Excel「单位」列：住宿人员所属单位。 */
    @field:ExcelProperty(value = ["单位"], index = 1)
    var unit: String? = null,

    /** 对应 Excel「房号」列：分配的房间号。 */
    @field:ExcelProperty(value = ["房号"], index = 2)
    var roomNumber: String? = null,

    /** 对应 Excel「类别」列：人员接待类别。 */
    @field:ExcelProperty(value = ["类别"], index = 3)
    var position: String? = null,
)
