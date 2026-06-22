package top.foxball.receptionbackendsystem.datasource.jdbc

/**
 * 提示服务配置数据仓库。
 */
interface PromptServiceRepository : ReceptionRepository<PromptService, Int> {
    /**
     * 查询指定活动下的全部提示服务配置。
     */
    fun findByActivityId(activityId: Int): List<PromptService>
}
