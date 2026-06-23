package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTagRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.LodgingRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.PersonRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ColorTagService

@Service
class ColorTagServiceImpl(
    private val colorTagRepository: ColorTagRepository,
    private val activitiesRepository: ActivitiesRepository,
    private val personRepository: PersonRepository,
    private val lodgingRepository: LodgingRepository,
) : AbstractReceptionService<ColorTag, Int>(colorTagRepository), ColorTagService {
    @Transactional
    override fun saveByActivity(activityId: Long, colorTags: List<ColorTag>): List<ColorTag> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingColorTagsById = activity.colorTagList.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()

        val normalizedColorTags = colorTags.map { colorTag ->
            val targetColorTag = colorTag.id?.let(existingColorTagsById::get) ?: ColorTag()
            targetColorTag.activity = activity
            targetColorTag.name = colorTag.name
            targetColorTag.color = colorTag.color
            targetColorTag.type = colorTag.type
            targetColorTag
        }

        return colorTagRepository.saveAll(normalizedColorTags).toList()
    }

    @Transactional
    override fun deleteOne(entity: ColorTag) {
        deleteBatch(listOf(entity))
    }

    @Transactional
    override fun deleteBatch(entities: Iterable<ColorTag>) {
        val ids = entities.mapNotNull { it.id }.distinct()
        if (ids.isEmpty()) {
            return
        }

        ids.forEach { colorTagId ->
            personRepository.findByColorTagId(colorTagId).forEach { person ->
                person.colorTag = null
            }
            lodgingRepository.findByColorTagId(colorTagId).forEach { lodging ->
                lodging.colorTag = null
            }
        }
        personRepository.flush()
        lodgingRepository.flush()

        colorTagRepository.deleteAll(colorTagRepository.findAllById(ids))
    }

    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<ColorTag> =
        colorTagRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndType(activityId: Long, type: String): List<ColorTag> =
        colorTagRepository.findByActivityIdAndType(activityId, type)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndName(activityId: Long, name: String): ColorTag? =
        colorTagRepository.findFirstByActivityIdAndNameOrderByIdAsc(activityId, name)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndNameAndType(activityId: Long, name: String, type: String): ColorTag? =
        colorTagRepository.findByActivityIdAndNameAndType(activityId, name, type)

    @Transactional(readOnly = true)
    override fun existsByActivityIdAndName(activityId: Long, name: String): Boolean =
        colorTagRepository.existsByActivityIdAndName(activityId, name)

    @Transactional(readOnly = true)
    override fun existsByActivityIdAndNameAndType(activityId: Long, name: String, type: String): Boolean =
        colorTagRepository.existsByActivityIdAndNameAndType(activityId, name, type)
}
