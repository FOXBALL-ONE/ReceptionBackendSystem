package top.foxball.receptionbackendsystem.service

interface ActivityEntityService<T : Any, ID : Any> : ReceptionService<T, ID> {
    fun findByActivityId(activityId: Long): List<T>
}
