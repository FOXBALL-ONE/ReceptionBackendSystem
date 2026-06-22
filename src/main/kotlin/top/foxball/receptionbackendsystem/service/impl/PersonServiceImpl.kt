package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PersonRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.PersonService

@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Person, Int>(personRepository), PersonService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Person> =
        personRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByNameContaining(name: String): List<Person> =
        personRepository.findByNameContaining(name)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndUnit(activityId: Long, unit: String): List<Person> =
        personRepository.findByActivityIdAndUnit(activityId, unit)

    @Transactional
    override fun saveByActivity(activityId: Long, persons: List<Person>): List<Person> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingPersonsById = activity.personList.mapNotNull { person ->
            person.id?.let { it to person }
        }.toMap()

        val normalizedPersons = persons.map { person ->
            val targetPerson = person.id?.let(existingPersonsById::get) ?: Person()
            targetPerson.activity = activity
            targetPerson.name = person.name
            targetPerson.unit = person.unit
            targetPerson.nickName = person.nickName
            targetPerson.inspectionTeamItemId = person.inspectionTeamItemId
            targetPerson
        }

        activity.personList.clear()
        activity.personList.addAll(normalizedPersons)

        return activitiesRepository.saveAndFlush(activity).personList
    }
}
