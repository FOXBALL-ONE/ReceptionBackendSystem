package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Person

interface PersonService : ActivityEntityService<Person, Int> {
    fun findByNameContaining(name: String): List<Person>

    fun findByActivityIdAndUnit(activityId: Int, unit: String): List<Person>
}
