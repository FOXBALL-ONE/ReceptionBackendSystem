package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ActivitiesService

/**
 * 活动服务实现，操作 [Activities] 实体本身。
 *
 * 活动是各业务子集合（日程、车辆、人员、用餐等）的聚合根，子集合的覆盖保存由各自服务承担。
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * 本实现仅维护活动基础字段（标题、时间、Banner、开关状态等），不直接触碰子集合。
 */
@Service
class ActivitiesServiceImpl(
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Activities, Long>(activitiesRepository), ActivitiesService {
    /** 查询全部活动，按开始时间升序。 */
    @Transactional(readOnly = true)
    override fun findAllActivities(): List<Activities> =
        activitiesRepository.findAllByOrderByStartTimeAsc()

    /** 按活动 id 查询单个活动。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): Activities? =
        activitiesRepository.findByActivityId(activityId)

    /** 按访问地址（url）查询活动，用于前台发布页路由定位。 */
    @Transactional(readOnly = true)
    override fun findByUrl(url: String): Activities? =
        activitiesRepository.findByUrl(url)

    /** 判断给定 url 是否已被活动占用。 */
    @Transactional(readOnly = true)
    override fun existsByUrl(url: String): Boolean =
        activitiesRepository.existsByUrl(url)

    /** 查询所有已对外开放的活动，按开始时间升序。 */
    @Transactional(readOnly = true)
    override fun findOpenActivities(): List<Activities> =
        activitiesRepository.findByIsOpenTrueOrderByStartTimeAsc()

    /** 切换活动的对外开放开关。 */
    @Transactional
    override fun updateIsOpen(id: Long, isOpen: Boolean): Activities {
        val activity = activitiesRepository.findEntityById(id)
            ?: throw ResourceNotFoundException("activity not found")
        activity.isOpen = isOpen
        return activitiesRepository.saveAndFlush(activity)
    }

    /** 新建活动，仅写入基础字段（忽略子集合），由 [copyBasicFieldsFrom] 完成字段拷贝。 */
    @Transactional
    override fun saveBasic(activity: Activities): Activities =
        activitiesRepository.saveAndFlush(Activities().apply { copyBasicFieldsFrom(activity) })

    /** 更新既有活动的基础字段（忽略子集合）。 */
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
