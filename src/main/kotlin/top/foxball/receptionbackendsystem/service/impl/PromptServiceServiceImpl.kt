package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptServiceRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PromptServiceService

/**
 * 提示服务配置实现，操作 [PromptService] 实体。
 *
 * 每个活动至多保留一份提示服务配置（含人员名单、注意事项、天气、考勤说明等）。
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的提示服务配置：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被替换的旧配置。
 */
@Service
class PromptServiceServiceImpl(
    private val promptServiceRepository: PromptServiceRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<PromptService, Int>(promptServiceRepository), PromptServiceService {
    /** 按活动查询其下提示服务配置列表。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<PromptService> =
        promptServiceRepository.findByActivityId(activityId)

    /**
     * 整体覆盖保存某活动的提示服务配置（单条）。
     *
     * 步骤：1) 加载活动并按 id 建立既有配置索引；
     * 2) 目标实体优先按 id 命中复用，否则取活动下已有首条，再否则新建；
     * 3) 回填活动引用与人员名单、注意事项、天气、考勤说明等字段；
     * 4) 清空 [Activities.promptServiceList] 后仅写入归一化后的这一条并保存活动。
     */
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
