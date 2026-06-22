package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCountyRepository
import top.foxball.receptionbackendsystem.service.OverviewOfTheCityAndCountyService

@Service
class OverviewOfTheCityAndCountyServiceImpl(
    private val overviewRepository: OverviewOfTheCityAndCountyRepository,
) : AbstractReceptionService<OverviewOfTheCityAndCounty, Int>(overviewRepository), OverviewOfTheCityAndCountyService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<OverviewOfTheCityAndCounty> =
        overviewRepository.findByActivityId(activityId)
}
