package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTagRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ColorTagService

/**
 * 颜色标签业务服务实现。
 */
@Service
@Transactional
class ColorTagServiceImpl(
    private val colorTagRepository: ColorTagRepository,
) : ColorTagService {

    /** 查询全部颜色标签。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<ColorTag> = colorTagRepository.findAll()

    /** 根据主键查询颜色标签。 */
    @Transactional(readOnly = true)
    override fun findById(id: Int): ColorTag = colorTagRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("颜色标签不存在：$id") }

    /** 根据名称查询颜色标签。 */
    @Transactional(readOnly = true)
    override fun findByName(name: String): ColorTag = colorTagRepository.findByName(name)
        ?: throw ResourceNotFoundException("颜色标签不存在：$name")

    /** 创建颜色标签。 */
    override fun create(colorTag: ColorTag): ColorTag {
        colorTag.id = null
        return colorTagRepository.save(colorTag)
    }

    /** 更新颜色标签。 */
    override fun update(id: Int, colorTag: ColorTag): ColorTag {
        if (!colorTagRepository.existsById(id)) {
            throw ResourceNotFoundException("颜色标签不存在：$id")
        }

        colorTag.id = id
        return colorTagRepository.save(colorTag)
    }

    /** 删除颜色标签。 */
    override fun delete(id: Int) {
        if (!colorTagRepository.existsById(id)) {
            throw ResourceNotFoundException("颜色标签不存在：$id")
        }

        colorTagRepository.deleteById(id)
    }
}
