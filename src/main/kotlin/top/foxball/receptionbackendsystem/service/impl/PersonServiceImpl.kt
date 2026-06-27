package top.foxball.receptionbackendsystem.service.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PersonRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PersonService

/**
 * 人员服务实现，操作 [Person] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的人员列表：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的人员。覆盖保存过程中会回填人员颜色分组（按 id/名称/颜色多重匹配）。
 */
@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Person, Int>(personRepository), PersonService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    /** 按活动查询其下全部人员，并记录查询日志与样本 id。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Person> {
        log.info("开始查询人员列表: activityId={}", activityId)
        val persons = personRepository.findByActivityId(activityId)
        log.info(
            "人员列表查询成功: activityId={}, personCount={}, personIds={}",
            activityId,
            persons.size,
            persons.sampleIds(),
        )
        return persons
    }

    /** 按姓名关键字模糊匹配人员。 */
    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<Person> =
        personRepository.findByNameContaining(name)

    /** 按活动与单位查询人员。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndUnit(activityId: Long, unit: String): List<Person> =
        personRepository.findByActivityIdAndUnit(activityId, unit)

    /**
     * 整体覆盖保存某活动的人员列表。
     *
     * 步骤：1) 加载活动并按 id 建立既有人员索引、活动下人员类型颜色分组索引；
     * 2) 逐人归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与字段；
     *    颜色分组按 [ColorTag.resolveIn] 在 id/名称/颜色之间多重匹配；
     * 3) 清空 [Activities.personList] 后整体替换为归一化结果并保存活动。
     * 全程记录关键步骤日志与样本 id，便于排障。
     */
    @Transactional
    override fun saveByActivity(activityId: Long, persons: List<Person>): List<Person> {
        log.info(
            "开始保存人员列表: activityId={}, requestPersonCount={}, requestPersonIds={}",
            activityId,
            persons.size,
            persons.sampleIds(),
        )
        val activity = activitiesRepository.findEntityById(activityId)
        if (activity == null) {
            log.warn("保存人员列表失败: 活动不存在, activityId={}", activityId)
            throw ResourceNotFoundException("活动不存在")
        }

        val existingPersonsById = activity.personList.mapNotNull { person ->
            person.id?.let { it to person }
        }.toMap()
        val personColorTags = activity.colorTagList.filter { it.isPersonType() }
        val existingColorTagsById = personColorTags.mapNotNull { colorTag ->
            colorTag.id?.let { it to colorTag }
        }.toMap()
        log.info(
            "人员保存活动数据加载完成: activityId={}, existingPersonCount={}, personColorTagCount={}",
            activityId,
            existingPersonsById.size,
            personColorTags.size,
        )

        val normalizedPersons = persons.map { person ->
            val targetPerson = person.id?.let(existingPersonsById::get) ?: Person()
            targetPerson.activity = activity
            targetPerson.name = person.name
            targetPerson.unit = person.unit
            targetPerson.nickName = person.nickName
            targetPerson.colorTag = person.colorTag?.resolveIn(existingColorTagsById, personColorTags)
            targetPerson.inspectionTeamItemId = person.inspectionTeamItemId
            targetPerson
        }
        log.info(
            "人员保存数据归一化完成: activityId={}, normalizedPersonCount={}, normalizedPersonIds={}",
            activityId,
            normalizedPersons.size,
            normalizedPersons.sampleIds(),
        )

        activity.personList.clear()
        activity.personList.addAll(normalizedPersons)

        val savedPersons = activitiesRepository.saveAndFlush(activity).personList
        log.info(
            "人员列表保存成功: activityId={}, savedPersonCount={}, savedPersonIds={}",
            activityId,
            savedPersons.size,
            savedPersons.sampleIds(),
        )
        return savedPersons
    }

    private fun ColorTag.resolveIn(
        colorTagsById: Map<Int, ColorTag>,
        colorTags: List<ColorTag>,
    ): ColorTag? =
        id?.let(colorTagsById::get)
            ?: name?.let { colorTagName -> colorTags.firstOrNull { it.name == colorTagName } }
            ?: color?.let { colorValue -> colorTags.firstOrNull { it.color == colorValue } }

    private fun ColorTag.isPersonType(): Boolean =
        type.isNullOrBlank() || type.equals(ColorTag.TYPE_PERSON, ignoreCase = true)

    private fun List<Person>.sampleIds(): List<Int?> =
        take(LOG_SAMPLE_SIZE).map { it.id }

    companion object {
        private const val LOG_SAMPLE_SIZE = 10
    }
}
