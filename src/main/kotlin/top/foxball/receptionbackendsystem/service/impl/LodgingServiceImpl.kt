package top.foxball.receptionbackendsystem.service.impl

import org.slf4j.LoggerFactory
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

/**
 * 住宿服务实现，操作 [Lodging] 实体。
 *
 * 住宿与住宿类型颜色分组 [ColorTag]（TYPE_LODGING）一同按活动整体覆盖保存，
 * 同时保留活动下其它类型（如人员）的颜色分组不被影响。
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 借助活动实体的 orphanRemoval 级联清理被移除的住宿记录与旧住宿分组。
 */
@Service
class LodgingServiceImpl(
    private val lodgingRepository: LodgingRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Lodging, Int>(lodgingRepository), LodgingService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    /** 按活动查询其下全部住宿，并记录查询日志与样本 id。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Lodging> {
        log.info("开始查询住宿列表: activityId={}", activityId)
        val lodgings = lodgingRepository.findByActivityId(activityId)
        log.info(
            "住宿列表查询成功: activityId={}, lodgingCount={}, lodgingIds={}",
            activityId,
            lodgings.size,
            lodgings.sampleLodgingIds(),
        )
        return lodgings
    }

    /**
     * 整体覆盖保存某活动的住宿列表及其颜色分组。
     *
     * 步骤：
     * 1) 加载活动，将颜色分组拆为「非住宿类型（保留）」与「既有住宿类型」两份，并建立住宿分组的 id/名称/颜色多重索引；
     *    请求中混入的非住宿类型分组按名称或颜色回退匹配住宿分组并告警；
     * 2) 归一化颜色分组：优先按名称/颜色在「本次归一化结果」与「既有住宿分组」间匹配复用，再回退 id 命中，最后新建；
     *    维护原始请求到归一化实体的映射，供后续住宿引用解析；
     * 3) 归一化住宿：按 id 复用既有实体做更新，否则新建；房间号、人员快照按请求覆盖，
     *    分组引用经 [ColorTag.resolveIn] 在「原始映射 / id / 名称 / 颜色」间多重解析；
     * 4) 清空 [Activities.hostedList] 与 [Activities.colorTagList]，先写回保留的非住宿分组，再追加归一化住宿分组与住宿；
     * 5) 保存活动后返回住宿分组与住宿两份结果。
     * 全程记录关键步骤日志与样本 id，便于排障。
     */
    @Transactional
    override fun saveByActivity(
        activityId: Long,
        colorTags: List<ColorTag>,
        lodgings: List<Lodging>,
    ): LodgingSaveResult {
        log.info(
            "开始保存住宿列表: activityId={}, requestColorTagCount={}, requestColorTagIds={}, requestLodgingCount={}, requestLodgingIds={}",
            activityId,
            colorTags.size,
            colorTags.sampleColorTagIds(),
            lodgings.size,
            lodgings.sampleLodgingIds(),
        )
        val activity = activitiesRepository.findEntityById(activityId)
        if (activity == null) {
            log.warn("保存住宿列表失败: 活动不存在, activityId={}", activityId)
            throw ResourceNotFoundException("活动不存在")
        }

        val preservedColorTags = activity.colorTagList.filterNot { it.isLodgingType() }
        val existingLodgingColorTags = activity.colorTagList.filter { it.isLodgingType() }
        val existingColorTagsById = existingLodgingColorTags.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()
        val existingColorTagsByName = existingLodgingColorTags.associateByName()
        val existingColorTagsByColor = existingLodgingColorTags.associateByColor()
        val existingLodgingsById = activity.hostedList.mapNotNull { lodging ->
            lodging.id?.let { it to lodging }
        }.toMap()
        log.info(
            "住宿保存活动数据加载完成: activityId={}, preservedColorTagCount={}, existingLodgingColorTagCount={}, existingLodgingCount={}",
            activityId,
            preservedColorTags.size,
            existingColorTagsById.size,
            existingLodgingsById.size,
        )
        val nonLodgingRequestColorTagIds = colorTags.filterNot { it.isLodgingType() }.mapNotNull { it.id }
        if (nonLodgingRequestColorTagIds.isNotEmpty()) {
            log.warn(
                "住宿保存请求包含非住宿颜色分组，将按名称或颜色匹配住宿分组: activityId={}, colorTagIds={}",
                activityId,
                nonLodgingRequestColorTagIds,
            )
        }

        val originalToNormalizedColorTags = IdentityHashMap<ColorTag, ColorTag>()
        val normalizedColorTags = mutableListOf<ColorTag>()
        val normalizedColorTagsByName = linkedMapOf<String, ColorTag>()
        val normalizedColorTagsByColor = linkedMapOf<String, ColorTag>()
        val sourceColorTagsById = mutableMapOf<Int, ColorTag>()
        colorTags.forEach { colorTag ->
            val targetColorTag = colorTag.findMatchedLodgingColorTag(
                normalizedColorTagsByName,
                normalizedColorTagsByColor,
                existingColorTagsByName,
                existingColorTagsByColor,
            )
                ?: colorTag.id?.let(existingColorTagsById::get)
                ?: ColorTag()
            targetColorTag.activity = activity
            targetColorTag.name = colorTag.name
            targetColorTag.color = colorTag.color
            targetColorTag.type = ColorTag.TYPE_LODGING
            originalToNormalizedColorTags[colorTag] = targetColorTag
            colorTag.id?.let { sourceColorTagsById[it] = targetColorTag }
            if (normalizedColorTags.none { it === targetColorTag }) {
                normalizedColorTags.add(targetColorTag)
            }
            targetColorTag.name.lookupKey()?.let { normalizedColorTagsByName.putIfAbsent(it, targetColorTag) }
            targetColorTag.color.lookupKey()?.let { normalizedColorTagsByColor.putIfAbsent(it, targetColorTag) }
        }
        val normalizedColorTagsById = normalizedColorTags.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()
        log.info(
            "住宿颜色标签归一化完成: activityId={}, normalizedColorTagCount={}, normalizedColorTagIds={}",
            activityId,
            normalizedColorTags.size,
            normalizedColorTags.sampleColorTagIds(),
        )

        val normalizedLodgings = lodgings.map { lodging ->
            val targetLodging = lodging.id?.let(existingLodgingsById::get) ?: Lodging()
            targetLodging.activity = activity
            targetLodging.roomNumber = lodging.roomNumber
            targetLodging.person = lodging.person?.toJsonSnapshot()
            targetLodging.colorTag = lodging.colorTag?.resolveIn(
                originalToNormalizedColorTags,
                sourceColorTagsById,
                normalizedColorTagsById,
                normalizedColorTags,
            )
            targetLodging
        }
        log.info(
            "住宿保存数据归一化完成: activityId={}, normalizedLodgingCount={}, normalizedLodgingIds={}",
            activityId,
            normalizedLodgings.size,
            normalizedLodgings.sampleLodgingIds(),
        )

        activity.hostedList.clear()
        activity.colorTagList.clear()
        activity.colorTagList.addAll(preservedColorTags)
        activity.colorTagList.addAll(normalizedColorTags)
        activity.hostedList.addAll(normalizedLodgings)

        val savedActivity = activitiesRepository.saveAndFlush(activity)
        val result = LodgingSaveResult(
            colorTags = savedActivity.colorTagList.filter { it.isLodgingType() },
            lodgings = savedActivity.hostedList,
        )
        log.info(
            "住宿列表保存成功: activityId={}, savedColorTagCount={}, savedColorTagIds={}, savedLodgingCount={}, savedLodgingIds={}",
            activityId,
            result.colorTags.size,
            result.colorTags.sampleColorTagIds(),
            result.lodgings.size,
            result.lodgings.sampleLodgingIds(),
        )
        return result
    }

    private fun ColorTag.resolveIn(
        originalToNormalizedColorTags: Map<ColorTag, ColorTag>,
        sourceColorTagsById: Map<Int, ColorTag>,
        colorTagsById: Map<Int, ColorTag>,
        colorTags: List<ColorTag>,
    ): ColorTag? =
        originalToNormalizedColorTags[this]
            ?: id?.let(sourceColorTagsById::get)
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

    private fun ColorTag.findMatchedLodgingColorTag(
        normalizedColorTagsByName: Map<String, ColorTag>,
        normalizedColorTagsByColor: Map<String, ColorTag>,
        existingColorTagsByName: Map<String, ColorTag>,
        existingColorTagsByColor: Map<String, ColorTag>,
    ): ColorTag? =
        name.lookupKey()?.let { key -> normalizedColorTagsByName[key] ?: existingColorTagsByName[key] }
            ?: color.lookupKey()?.let { key -> normalizedColorTagsByColor[key] ?: existingColorTagsByColor[key] }

    private fun Iterable<ColorTag>.associateByName(): Map<String, ColorTag> =
        associateFirstBy { it.name.lookupKey() }

    private fun Iterable<ColorTag>.associateByColor(): Map<String, ColorTag> =
        associateFirstBy { it.color.lookupKey() }

    private fun Iterable<ColorTag>.associateFirstBy(keySelector: (ColorTag) -> String?): Map<String, ColorTag> {
        val result = linkedMapOf<String, ColorTag>()
        for (colorTag in this) {
            val key = keySelector(colorTag) ?: continue
            result.putIfAbsent(key, colorTag)
        }
        return result
    }

    private fun String?.lookupKey(): String? =
        this?.trim()?.lowercase()?.takeIf { it.isNotBlank() }

    private fun List<Lodging>.sampleLodgingIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    private fun List<ColorTag>.sampleColorTagIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    companion object {
        private const val LOG_SAMPLE_SIZE = 10
    }
}
