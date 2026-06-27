package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty

/**
 * 市县概况业务服务契约。
 *
 * 为 [OverviewOfTheCityAndCounty] 提供按活动维度查询及整体覆盖保存概况列表。
 */
interface OverviewOfTheCityAndCountyService : ActivityEntityService<OverviewOfTheCityAndCounty, Int> {
    /**
     * 整体覆盖保存活动下的市县概况列表。
     *
     * 以 [activityId] 为维度用 [overviews] 覆盖该活动原有概况记录。
     */
    fun saveByActivity(
        activityId: Long,
        overviews: List<OverviewOfTheCityAndCounty>,
    ): List<OverviewOfTheCityAndCounty>
}
