package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PersonRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PersonService

/**
 * 活动人员业务服务实现。
 */
@Service
@Transactional
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val activitiesRepository: ActivitiesRepository,
) : PersonService {

    /** 查询全部人员。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<Person> = personRepository.findAll()

    /** 根据主键查询人员。 */
    @Transactional(readOnly = true)
    override fun findById(id: Int): Person = personRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("人员不存在：$id") }

    /** 查询指定活动下的人员列表。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Person> = personRepository.findByActivityId(activityId)

    /** 根据姓名模糊查询人员。 */
    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<Person> = personRepository.findByNameContaining(name)

    /** 查询指定活动下指定单位的人员。 */
    @Transactional(readOnly = true)
    override fun findByActivityIdAndUnit(activityId: Int, unit: String): List<Person> =
        personRepository.findByActivityIdAndUnit(activityId, unit)

    /** 在指定活动下创建人员。 */
    override fun create(activityId: Int, person: Person): Person {
        person.id = null
        person.activity = findActivity(activityId)
        return personRepository.save(person)
    }

    /** 更新人员信息，并保留或重设所属活动关系。 */
    override fun update(id: Int, person: Person): Person {
        val existing = findById(id)
        val activity = person.activity?.id?.let(::findActivity)
            ?: existing.activity
            ?: throw ResourceNotFoundException("人员所属活动不存在")

        person.id = id
        person.activity = activity
        return personRepository.save(person)
    }

    /** 删除人员。 */
    override fun delete(id: Int) {
        if (!personRepository.existsById(id)) {
            throw ResourceNotFoundException("人员不存在：$id")
        }

        personRepository.deleteById(id)
    }

    /** 批量保存人员，先删除活动下的旧人员，再批量插入新人员。 */
    override fun batchSave(activityId: Int, persons: List<Person>): List<Person> {
        val activity = findActivity(activityId)

        // 删除该活动下的所有旧人员
        val oldPersons = personRepository.findByActivityId(activityId)
        personRepository.deleteAll(oldPersons)

        // 批量插入新人员
        val newPersons = persons.map { person ->
            person.id = null
            person.activity = activity
            person
        }

        return personRepository.saveAll(newPersons)
    }

    /** 查询人员所属活动。 */
    private fun findActivity(activityId: Int): Activities = activitiesRepository.findById(activityId)
        .orElseThrow { ResourceNotFoundException("活动不存在：$activityId") }
}
