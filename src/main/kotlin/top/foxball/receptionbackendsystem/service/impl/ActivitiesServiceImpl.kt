package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.service.ActivitiesService

@Service
class ActivitiesServiceImpl(
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Activities, Long>(activitiesRepository), ActivitiesService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): Activities? =
        activitiesRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByUrl(url: String): Activities? =
        activitiesRepository.findByUrl(url)

    @Transactional(readOnly = true)
    override fun existsByUrl(url: String): Boolean =
        activitiesRepository.existsByUrl(url)

    @Transactional(readOnly = true)
    override fun findOpenActivities(): List<Activities> =
        activitiesRepository.findByIsOpenTrueOrderByStartTimeAsc()
}
