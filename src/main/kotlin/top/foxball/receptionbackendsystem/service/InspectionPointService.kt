package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint

interface InspectionPointService : ActivityEntityService<InspectionPoint, Int> {
    fun saveByActivity(activityId: Long, inspectionPoints: List<InspectionPoint>): List<InspectionPoint>
}
