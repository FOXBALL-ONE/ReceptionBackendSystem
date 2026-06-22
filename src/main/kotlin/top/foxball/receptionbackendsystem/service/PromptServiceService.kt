package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService

interface PromptServiceService : ActivityEntityService<PromptService, Int> {
    fun saveByActivity(activityId: Long, promptService: PromptService): PromptService
}
