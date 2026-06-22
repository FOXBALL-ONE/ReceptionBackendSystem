package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PersonRepository
import top.foxball.receptionbackendsystem.service.PersonService

@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
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
}
