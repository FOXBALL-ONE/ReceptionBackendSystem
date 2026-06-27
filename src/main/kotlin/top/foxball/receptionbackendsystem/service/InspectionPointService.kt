package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint

/**
 * 考察点业务服务契约。
 *
 * 为 [InspectionPoint] 提供按活动维度查询及整体覆盖保存考察点列表。
 */
interface InspectionPointService : ActivityEntityService<InspectionPoint, Int> {
    /**
     * 整体覆盖保存活动下的考察点列表。
     *
     * 以 [activityId] 为维度用 [inspectionPoints] 覆盖该活动原有考察点。
     */
    fun saveByActivity(activityId: Long, inspectionPoints: List<InspectionPoint>): List<InspectionPoint>
}
