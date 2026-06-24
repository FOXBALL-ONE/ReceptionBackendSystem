package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ActivitiesService

@Service
class ActivitiesServiceImpl(
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Activities, Long>(activitiesRepository), ActivitiesService {
    @Transactional(readOnly = true)
    override fun findAllActivities(): List<Activities> =
        activitiesRepository.findAllByOrderByStartTimeAsc()

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

    @Transactional
    override fun updateIsOpen(id: Long, isOpen: Boolean): Activities {
        val activity = activitiesRepository.findEntityById(id)
            ?: throw ResourceNotFoundException("activity not found")
        activity.isOpen = isOpen
        return activitiesRepository.saveAndFlush(activity)
    }

    @Transactional
    override fun saveBasic(activity: Activities): Activities =
        activitiesRepository.saveAndFlush(Activities().apply { copyBasicFieldsFrom(activity) })

    @Transactional
    override fun updateBasic(id: Long, activity: Activities): Activities {
        val target = activitiesRepository.findEntityById(id)
            ?: throw ResourceNotFoundException("activity not found")

        target.copyBasicFieldsFrom(activity)
        return activitiesRepository.saveAndFlush(target)
    }

    private fun Activities.copyBasicFieldsFrom(source: Activities) {
        url = source.url
        masterTitle = source.masterTitle
        subtitle = source.subtitle
        name = source.name
        startTime = source.startTime
        endTime = source.endTime
        bannerTags = source.bannerTags
        bannerUrl = source.bannerUrl
        isAnimation = source.isAnimation
        isTopNavigationBar = source.isTopNavigationBar
        icp = source.icp
        technicalSupport = source.technicalSupport
        isOpen = source.isOpen
    }
}
