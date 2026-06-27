package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService

/**
 * 提示服务业务服务契约。
 *
 * 注意类名中的"Service"重复：本接口服务于 [PromptService] 实体（提示服务配置），
 * 为其提供按活动维度查询及整体覆盖保存。
 */
interface PromptServiceService : ActivityEntityService<PromptService, Int> {
    /**
     * 整体覆盖保存活动下的提示服务配置。
     *
     * 以 [activityId] 为维度用 [promptService] 覆盖该活动原有提示服务配置。
     */
    fun saveByActivity(activityId: Long, promptService: PromptService): PromptService
}
