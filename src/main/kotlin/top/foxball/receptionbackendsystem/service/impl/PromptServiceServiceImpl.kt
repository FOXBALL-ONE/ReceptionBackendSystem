package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptServiceRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PromptServiceService

@Service
class PromptServiceServiceImpl(
    private val promptServiceRepository: PromptServiceRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<PromptService, Int>(promptServiceRepository), PromptServiceService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<PromptService> =
        promptServiceRepository.findByActivityId(activityId)

    @Transactional
    override fun saveByActivity(activityId: Long, promptService: PromptService): PromptService {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingPromptServicesById = activity.promptServiceList.mapNotNull { item ->
            item.id?.let { it to item }
        }.toMap()

        val targetPromptService = promptService.id?.let(existingPromptServicesById::get)
            ?: activity.promptServiceList.firstOrNull()
            ?: PromptService()

        targetPromptService.activity = activity
        targetPromptService.staffList = promptService.staffList.toMutableList()
        targetPromptService.noteList = promptService.noteList.toMutableList()
        targetPromptService.weatherList = promptService.weatherList.toMutableList()
        targetPromptService.attendanceInstructionsMode = promptService.attendanceInstructionsMode
        targetPromptService.attendanceInstructionsTitle = promptService.attendanceInstructionsTitle
        targetPromptService.attendanceInstructionsContent = promptService.attendanceInstructionsContent

        activity.promptServiceList.clear()
        activity.promptServiceList.add(targetPromptService)

        return activitiesRepository.saveAndFlush(activity).promptServiceList.first()
    }
}
