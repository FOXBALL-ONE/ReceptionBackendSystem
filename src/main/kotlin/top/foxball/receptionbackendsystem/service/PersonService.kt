package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Person

interface PersonService : ActivityEntityService<Person, Int> {
    fun findByNameContaining(name: String): List<Person>

    fun findByActivityIdAndUnit(activityId: Long, unit: String): List<Person>

    fun saveByActivity(activityId: Long, persons: List<Person>): List<Person>
}
