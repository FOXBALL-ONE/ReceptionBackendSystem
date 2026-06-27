package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 考察点的 Excel 行模型。
 *
 * 对应活动下属的考察点记录（InspectionPoint 实体 / activity 关联），用于考察点信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为名称、简介。
 */
data class InspectionPointsItem(

    /** 对应 Excel「名称」列：考察点名称。 */
    @field:ExcelProperty(value = ["名称"], index = 0)
    var name: String? = null,

    /** 对应 Excel「简介」列：考察点情况说明。 */
    @field:ExcelProperty(value = ["简介"], index = 1)
    var description: String? = null,
)
