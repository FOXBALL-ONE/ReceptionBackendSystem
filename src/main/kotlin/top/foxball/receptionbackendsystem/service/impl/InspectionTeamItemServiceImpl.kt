package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItemRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.InspectionTeamItemService

/**
 * 考察组安排业务服务实现。
 */
@Service
@Transactional
class InspectionTeamItemServiceImpl(
    private val inspectionTeamItemRepository: InspectionTeamItemRepository,
) : InspectionTeamItemService {

    /** 查询全部考察组安排。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<InspectionTeamItem> = inspectionTeamItemRepository.findAll()

    /** 根据主键查询考察组安排。 */
    @Transactional(readOnly = true)
    override fun findById(id: Long): InspectionTeamItem = inspectionTeamItemRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("考察组安排不存在：$id") }

    /** 根据考察组名称模糊查询。 */
    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<InspectionTeamItem> =
        inspectionTeamItemRepository.findByNameContaining(name)

    /** 创建考察组安排。 */
    override fun create(inspectionTeamItem: InspectionTeamItem): InspectionTeamItem {
        inspectionTeamItem.id = null
        return inspectionTeamItemRepository.save(inspectionTeamItem)
    }

    /** 更新考察组安排。 */
    override fun update(id: Long, inspectionTeamItem: InspectionTeamItem): InspectionTeamItem {
        if (!inspectionTeamItemRepository.existsById(id)) {
            throw ResourceNotFoundException("考察组安排不存在：$id")
        }

        inspectionTeamItem.id = id
        return inspectionTeamItemRepository.save(inspectionTeamItem)
    }

    /** 删除考察组安排。 */
    override fun delete(id: Long) {
        if (!inspectionTeamItemRepository.existsById(id)) {
            throw ResourceNotFoundException("考察组安排不存在：$id")
        }

        inspectionTeamItemRepository.deleteById(id)
    }
}
