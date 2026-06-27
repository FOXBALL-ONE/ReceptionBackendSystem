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

/**
 * 颜色标签服务实现，操作 [ColorTag] 实体。
 *
 * 颜色标签被人员、住宿等多处引用，删除时需先解绑引用再物理删除，避免外键残留。
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的颜色标签集合：按 id 复用既有实体做更新，否则新建。
 */
@Service
class ColorTagServiceImpl(
    private val colorTagRepository: ColorTagRepository,
    private val activitiesRepository: ActivitiesRepository,
    private val personRepository: PersonRepository,
    private val lodgingRepository: LodgingRepository,
) : AbstractReceptionService<ColorTag, Int>(colorTagRepository), ColorTagService {
    /**
     * 整体覆盖保存某活动的颜色标签集合。
     *
     * 步骤：1) 加载活动并按 id 建立既有颜色标签索引；
     * 2) 逐项归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与名称、颜色、类型；
     * 3) 通过 [ColorTagRepository.saveAll] 持久化归一化结果。
     */
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

    /** 删除单个颜色标签：委托给 [deleteBatch]，先解绑引用再删除。 */
    @Transactional
    override fun deleteOne(entity: ColorTag) {
        deleteBatch(listOf(entity))
    }

    /**
     * 批量删除颜色标签。
     *
     * 删除前先将引用了这些标签的人员 [Person.colorTag]、住宿 [Lodging.colorTag] 置空并 flush，
     * 避免外键约束违反；随后再物理删除标签本身。
     */
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

    /** 按活动查询其下全部颜色标签。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<ColorTag> =
        colorTagRepository.findByActivityId(activityId)

    /** 按活动与类型查询颜色标签。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndType(activityId: Long, type: String): List<ColorTag> =
        colorTagRepository.findByActivityIdAndType(activityId, type)

    /** 按活动与名称查询首个颜色标签（按 id 升序）。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndName(activityId: Long, name: String): ColorTag? =
        colorTagRepository.findFirstByActivityIdAndNameOrderByIdAsc(activityId, name)

    /** 按活动、名称与类型精确定位颜色标签。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndNameAndType(activityId: Long, name: String, type: String): ColorTag? =
        colorTagRepository.findByActivityIdAndNameAndType(activityId, name, type)

    /** 判断某活动下是否已存在同名颜色标签。 */
    @Transactional(readOnly = true)
    override fun existsByActivityIdAndName(activityId: Long, name: String): Boolean =
        colorTagRepository.existsByActivityIdAndName(activityId, name)

    /** 判断某活动下是否已存在同名且同类型的颜色标签。 */
    @Transactional(readOnly = true)
    override fun existsByActivityIdAndNameAndType(activityId: Long, name: String, type: String): Boolean =
        colorTagRepository.existsByActivityIdAndNameAndType(activityId, name, type)
}
