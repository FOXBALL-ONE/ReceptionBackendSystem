package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTagRepository
import top.foxball.receptionbackendsystem.service.ColorTagService

@Service
class ColorTagServiceImpl(
    private val colorTagRepository: ColorTagRepository,
) : AbstractReceptionService<ColorTag, Int>(colorTagRepository), ColorTagService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<ColorTag> =
        colorTagRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndName(activityId: Int, name: String): ColorTag? =
        colorTagRepository.findByActivityIdAndName(activityId, name)

    @Transactional(readOnly = true)
    override fun existsByActivityIdAndName(activityId: Int, name: String): Boolean =
        colorTagRepository.existsByActivityIdAndName(activityId, name)
}
