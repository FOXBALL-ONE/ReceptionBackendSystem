package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Activities

interface ActivitiesService : ReceptionService<Activities, Long> {
    fun findAllActivities(): List<Activities>

    fun findByActivityId(activityId: Long): Activities?

    fun findByUrl(url: String): Activities?

    fun existsByUrl(url: String): Boolean

    fun findOpenActivities(): List<Activities>
}
