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

@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Person, Int>(personRepository), PersonService {
    private val log = LoggerFactory.getLogger(this.javaClass)

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

    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<Person> =
        personRepository.findByNameContaining(name)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndUnit(activityId: Long, unit: String): List<Person> =
        personRepository.findByActivityIdAndUnit(activityId, unit)

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
