package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ActivitiesService

/**
 * 活动配置业务服务实现。
 */
@Service
@Transactional
class ActivitiesServiceImpl(
    private val activitiesRepository: ActivitiesRepository,
) : ActivitiesService {

    /** 查询全部活动配置。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<Activities> = activitiesRepository.findAll()

    /** 根据主键查询活动配置。 */
    @Transactional(readOnly = true)
    override fun findById(id: Int): Activities = activitiesRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("活动不存在：$id") }

    /** 根据活动访问地址查询活动配置。 */
    @Transactional(readOnly = true)
    override fun findByUrl(url: String): Activities = activitiesRepository.findByUrl(url)
        ?: throw ResourceNotFoundException("活动不存在：$url")

    /** 查询已开放活动列表。 */
    @Transactional(readOnly = true)
    override fun findOpenActivities(): List<Activities> = activitiesRepository.findByIsOpenTrueOrderByStartTimeAsc()

    /** 创建活动，并将子级日程、车辆反向绑定到当前活动。 */
    override fun create(activity: Activities): Activities {
        activity.id = null
        bindChildren(activity)
        return activitiesRepository.save(activity)
    }

    /** 更新活动，并重新绑定子级日程、车辆关系。 */
    override fun update(id: Int, activity: Activities): Activities {
        if (!activitiesRepository.existsById(id)) {
            throw ResourceNotFoundException("活动不存在：$id")
        }

        activity.id = id
        bindChildren(activity)
        return activitiesRepository.save(activity)
    }

    /** 删除活动配置。 */
    override fun delete(id: Int) {
        if (!activitiesRepository.existsById(id)) {
            throw ResourceNotFoundException("活动不存在：$id")
        }

        activitiesRepository.deleteById(id)
    }

    /** 维护 JPA 双向关系中的子级反向引用。 */
    private fun bindChildren(activity: Activities) {
        activity.schedules.forEach { it.activity = activity }
        activity.carList.forEach { it.activity = activity }
    }
}
