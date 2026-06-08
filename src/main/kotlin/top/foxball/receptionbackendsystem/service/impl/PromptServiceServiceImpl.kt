package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptServiceRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PromptServiceService

/**
 * 提示服务配置业务服务实现。
 */
@Service
@Transactional
class PromptServiceServiceImpl(
    private val promptServiceRepository: PromptServiceRepository,
) : PromptServiceService {

    /** 查询全部提示服务配置。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<PromptService> = promptServiceRepository.findAll()

    /** 根据主键查询提示服务配置。 */
    @Transactional(readOnly = true)
    override fun findById(id: Int): PromptService = promptServiceRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("提示服务配置不存在：$id") }

    /** 创建提示服务配置。 */
    override fun create(promptService: PromptService): PromptService {
        promptService.id = null
        return promptServiceRepository.save(promptService)
    }

    /** 更新提示服务配置。 */
    override fun update(id: Int, promptService: PromptService): PromptService {
        if (!promptServiceRepository.existsById(id)) {
            throw ResourceNotFoundException("提示服务配置不存在：$id")
        }

        promptService.id = id
        return promptServiceRepository.save(promptService)
    }

    /** 删除提示服务配置。 */
    override fun delete(id: Int) {
        if (!promptServiceRepository.existsById(id)) {
            throw ResourceNotFoundException("提示服务配置不存在：$id")
        }

        promptServiceRepository.deleteById(id)
    }
}
