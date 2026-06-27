package top.foxball.receptionbackendsystem.datasource.excel

import com.alibaba.excel.annotation.ExcelProperty

/**
 * 市县概况的 Excel 行模型。
 *
 * 对应活动下属的市县概况记录（OverviewOfTheCityAndCounty 实体 / activity 关联），按地区逐行描述基本情况，
 * 用于市县概况信息的 Excel 导入导出；
 * 列顺序由 `@ExcelProperty(index = ...)` 决定，依次为市县地区、基本情况。
 */
data class OverviewOfTheCityAndCountyItem(

    /** 对应 Excel「市县地区」列：市县名称或地区节点。 */
    @field:ExcelProperty(value = ["市县地区"], index = 0)
    var node: String? = null,

    /** 对应 Excel「基本情况」列：该市县的基本情况说明。 */
    @field:ExcelProperty(value = ["基本情况"], index = 1)
    var content: String? = null,
)

/**
 * 市县概况导出汇总的 Excel 行模型。
 *
 * 用于将整段市县概况文本以单列「市县概况」形式导出；列顺序由 `@ExcelProperty(index = ...)` 决定，仅有市县概况一列。
 */
data class OverviewOfTheCityAndCountyExcelRow(
    /** 对应 Excel「市县概况」列：概况文本内容。 */
    @field:ExcelProperty(value = ["市县概况"], index = 0)
    var value: String? = null,
)
