package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptServiceRepository
import top.foxball.receptionbackendsystem.service.PromptServiceService

@Service
class PromptServiceServiceImpl(
    private val promptServiceRepository: PromptServiceRepository,
) : AbstractReceptionService<PromptService, Int>(promptServiceRepository), PromptServiceService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<PromptService> =
        promptServiceRepository.findByActivityId(activityId)
}
