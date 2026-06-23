package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCountyRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.OverviewOfTheCityAndCountyService

@Service
class OverviewOfTheCityAndCountyServiceImpl(
    private val overviewRepository: OverviewOfTheCityAndCountyRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<OverviewOfTheCityAndCounty, Int>(overviewRepository), OverviewOfTheCityAndCountyService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<OverviewOfTheCityAndCounty> =
        overviewRepository.findByActivityId(activityId)

    @Transactional
    override fun saveByActivity(
        activityId: Long,
        overviews: List<OverviewOfTheCityAndCounty>,
    ): List<OverviewOfTheCityAndCounty> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingOverviewsById = activity.overviewOfTheCityAndCounty.mapNotNull { overview ->
            overview.id?.let { it to overview }
        }.toMap()

        val normalizedOverviews = overviews.map { overview ->
            val targetOverview = overview.id?.let(existingOverviewsById::get) ?: OverviewOfTheCityAndCounty()
            targetOverview.activity = activity
            targetOverview.title = overview.title
            targetOverview.topImageUrl = overview.topImageUrl
            targetOverview.description.clear()
            targetOverview.description.addAll(overview.description.map { it.copy() })
            targetOverview
        }

        activity.overviewOfTheCityAndCounty.clear()
        activity.overviewOfTheCityAndCounty.addAll(normalizedOverviews)

        return activitiesRepository.saveAndFlush(activity).overviewOfTheCityAndCounty
    }
}
