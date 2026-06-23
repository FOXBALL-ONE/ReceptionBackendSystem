package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.LodgingRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.LodgingSaveResult
import top.foxball.receptionbackendsystem.service.LodgingService
import java.util.IdentityHashMap

@Service
class LodgingServiceImpl(
    private val lodgingRepository: LodgingRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Lodging, Int>(lodgingRepository), LodgingService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Lodging> =
        lodgingRepository.findByActivityId(activityId)

    @Transactional
    override fun saveByActivity(
        activityId: Long,
        colorTags: List<ColorTag>,
        lodgings: List<Lodging>,
    ): LodgingSaveResult {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val preservedColorTags = activity.colorTagList.filterNot { it.isLodgingType() }
        val existingColorTagsById = activity.colorTagList.filter { it.isLodgingType() }.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()
        val existingLodgingsById = activity.hostedList.mapNotNull { lodging ->
            lodging.id?.let { it to lodging }
        }.toMap()

        val originalToNormalizedColorTags = IdentityHashMap<ColorTag, ColorTag>()
        val normalizedColorTags = colorTags.map { colorTag ->
            val targetColorTag = colorTag.id?.let(existingColorTagsById::get) ?: ColorTag()
            targetColorTag.activity = activity
            targetColorTag.name = colorTag.name
            targetColorTag.color = colorTag.color
            targetColorTag.type = ColorTag.TYPE_LODGING
            originalToNormalizedColorTags[colorTag] = targetColorTag
            targetColorTag
        }
        val normalizedColorTagsById = normalizedColorTags.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()

        val normalizedLodgings = lodgings.map { lodging ->
            val targetLodging = lodging.id?.let(existingLodgingsById::get) ?: Lodging()
            targetLodging.activity = activity
            targetLodging.roomNumber = lodging.roomNumber
            targetLodging.person = lodging.person?.toJsonSnapshot()
            targetLodging.colorTag = lodging.colorTag?.resolveIn(
                originalToNormalizedColorTags,
                normalizedColorTagsById,
                normalizedColorTags,
            )
            targetLodging
        }

        activity.hostedList.clear()
        activity.colorTagList.clear()
        activity.colorTagList.addAll(preservedColorTags)
        activity.colorTagList.addAll(normalizedColorTags)
        activity.hostedList.addAll(normalizedLodgings)

        val savedActivity = activitiesRepository.saveAndFlush(activity)
        return LodgingSaveResult(
            colorTags = savedActivity.colorTagList.filter { it.isLodgingType() },
            lodgings = savedActivity.hostedList,
        )
    }

    private fun ColorTag.resolveIn(
        originalToNormalizedColorTags: Map<ColorTag, ColorTag>,
        colorTagsById: Map<Int, ColorTag>,
        colorTags: List<ColorTag>,
    ): ColorTag? =
        originalToNormalizedColorTags[this]
            ?: id?.let(colorTagsById::get)
            ?: name?.let { colorTagName -> colorTags.firstOrNull { it.name == colorTagName } }
            ?: color?.let { colorValue -> colorTags.firstOrNull { it.color == colorValue } }

    private fun ColorTag.isLodgingType(): Boolean =
        type.isNullOrBlank() || type.equals(ColorTag.TYPE_LODGING, ignoreCase = true)

    private fun Person.toJsonSnapshot(): Person =
        Person(
            id = id,
            name = name,
            unit = unit,
            nickName = nickName,
            inspectionTeamItemId = inspectionTeamItemId,
        )
}
