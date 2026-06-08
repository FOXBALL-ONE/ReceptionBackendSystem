package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService

/**
 * 提示服务配置业务服务。
 */
interface PromptServiceService {
    /** 查询全部提示服务配置。 */
    fun findAll(): List<PromptService>

    /** 根据主键查询提示服务配置，不存在时抛出业务异常。 */
    fun findById(id: Int): PromptService

    /** 创建提示服务配置。 */
    fun create(promptService: PromptService): PromptService

    /** 更新提示服务配置。 */
    fun update(id: Int, promptService: PromptService): PromptService

    /** 删除提示服务配置。 */
    fun delete(id: Int)
}
