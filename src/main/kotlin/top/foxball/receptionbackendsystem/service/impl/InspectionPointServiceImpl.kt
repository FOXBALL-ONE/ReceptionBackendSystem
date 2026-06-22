package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPointRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.InspectionPointService

@Service
class InspectionPointServiceImpl(
    private val inspectionPointRepository: InspectionPointRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<InspectionPoint, Int>(inspectionPointRepository), InspectionPointService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<InspectionPoint> =
        inspectionPointRepository.findByActivityId(activityId)

    @Transactional
    override fun saveByActivity(activityId: Long, inspectionPoints: List<InspectionPoint>): List<InspectionPoint> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingInspectionPointsById = activity.inspectionPoints.mapNotNull { inspectionPoint ->
            inspectionPoint.id?.let { it to inspectionPoint }
        }.toMap()

        val normalizedInspectionPoints = inspectionPoints.map { inspectionPoint ->
            val targetInspectionPoint = inspectionPoint.id?.let(existingInspectionPointsById::get) ?: InspectionPoint()
            targetInspectionPoint.activity = activity
            targetInspectionPoint.imageURL = inspectionPoint.imageURL
            targetInspectionPoint.description = inspectionPoint.description
            targetInspectionPoint
        }

        activity.inspectionPoints.clear()
        activity.inspectionPoints.addAll(normalizedInspectionPoints)

        return activitiesRepository.saveAndFlush(activity).inspectionPoints
    }
}
