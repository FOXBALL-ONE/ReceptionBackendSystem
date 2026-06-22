package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPointRepository
import top.foxball.receptionbackendsystem.service.InspectionPointService

@Service
class InspectionPointServiceImpl(
    private val inspectionPointRepository: InspectionPointRepository,
) : AbstractReceptionService<InspectionPoint, Int>(inspectionPointRepository), InspectionPointService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<InspectionPoint> =
        inspectionPointRepository.findByActivityId(activityId)
}
