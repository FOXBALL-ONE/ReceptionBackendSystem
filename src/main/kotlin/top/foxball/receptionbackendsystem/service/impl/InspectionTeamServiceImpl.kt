package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamRepository
import top.foxball.receptionbackendsystem.service.InspectionTeamService

@Service
class InspectionTeamServiceImpl(
    private val inspectionTeamRepository: InspectionTeamRepository,
) : AbstractReceptionService<InspectionTeamItem, Long>(inspectionTeamRepository), InspectionTeamService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<InspectionTeamItem> =
        inspectionTeamRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<InspectionTeamItem> =
        inspectionTeamRepository.findByNameContaining(name)
}
