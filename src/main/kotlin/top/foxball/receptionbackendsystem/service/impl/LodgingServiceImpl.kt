package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.LodgingRepository
import top.foxball.receptionbackendsystem.service.LodgingService

@Service
class LodgingServiceImpl(
    private val lodgingRepository: LodgingRepository,
) : AbstractReceptionService<Lodging, Int>(lodgingRepository), LodgingService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Lodging> =
        lodgingRepository.findByActivityId(activityId)
}
