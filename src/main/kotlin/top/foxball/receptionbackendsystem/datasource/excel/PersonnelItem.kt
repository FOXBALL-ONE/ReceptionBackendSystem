package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 人员名单的 Excel 行模型。
 *
 * 对应活动下属的接待人员名单（Person 实体 / activity 关联），用于活动人员信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为序号、姓名、单位、类别、考察组。
 */
data class PersonnelItem(

    /** 对应 Excel「序号」列：人员序号。 */
    @field:ExcelProperty(value = ["序号"], index = 0)
    var id: Long? = null,

    /** 对应 Excel「姓名」列：人员姓名。 */
    @field:ExcelProperty(value = ["姓名"], index = 1)
    var name: String? = null,

    /** 对应 Excel「单位」列：人员所属单位。 */
    @field:ExcelProperty(value = ["单位"], index = 2)
    var unit: String? = null,

    /** 对应 Excel「类别」列：人员接待类别（如职务/身份分类）。 */
    @field:ExcelProperty(value = ["类别"], index = 3)
    var position: String? = null,

    /** 对应 Excel「考察组」列：所属考察组名称。 */
    @field:ExcelProperty(value = ["考察组"], index = 4)
    var inspectionTeam: String? = null,
)
