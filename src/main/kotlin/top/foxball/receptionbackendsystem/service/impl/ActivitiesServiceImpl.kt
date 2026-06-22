package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ActivitiesService
import java.time.LocalDateTime

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

    /** 更新活动基础信息，并保留已有的子级关联数据。 */
    override fun update(id: Int, activity: Activities): Activities {
        val existing = activitiesRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("活动不存在：$id") }

        copyEditableFields(source = activity, target = existing)
        return activitiesRepository.save(existing)
    }

    /** 删除活动配置。 */
    override fun delete(id: Int) {
        if (!activitiesRepository.existsById(id)) {
            throw ResourceNotFoundException("活动不存在：$id")
        }

        activitiesRepository.deleteById(id)
    }

    /** 关闭活动。 */
    override fun closeActivity(id: Int): Activities {
        val activity = activitiesRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("活动不存在") }

        activity.isOpen = false
        return activitiesRepository.save(activity)
    }

    /** 开放活动。 */
    override fun openActivity(id: Int): Activities {
        val activity = activitiesRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("活动不存在") }

        activity.isOpen = true
        return activitiesRepository.save(activity)
    }

    /** 搜索活动。 */
    @Transactional(readOnly = true)
    override fun searchActivities(keyword: String?, status: String?): List<Activities> {
        var activities = activitiesRepository.findAll()

        if (!keyword.isNullOrBlank()) {
            val lowerKeyword = keyword.lowercase()
            activities = activities.filter {
                it.name?.lowercase()?.contains(lowerKeyword) == true ||
                it.url?.lowercase()?.contains(lowerKeyword) == true ||
                it.masterTitle?.lowercase()?.contains(lowerKeyword) == true ||
                it.subtitle?.lowercase()?.contains(lowerKeyword) == true
            }
        }

        if (!status.isNullOrBlank()) {
            activities = when (status) {
                "open" -> activities.filter { it.isOpen == true }
                "closed" -> activities.filter { it.isOpen == false }
                else -> activities
            }
        }

        return activities
    }

    /** 获取活动统计信息。 */
    @Transactional(readOnly = true)
    override fun getStatistics(): Map<String, Any> {
        val allActivities = activitiesRepository.findAll()
        val total = allActivities.size
        val open = allActivities.count { it.isOpen == true }
        val closed = allActivities.count { it.isOpen == false }
        val pending = total - open - closed

        val today = LocalDateTime.now().toLocalDate()
        val todayCreated = 0 // 需要添加创建时间字段才能统计

        val thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val thisMonthCreated = 0 // 需要添加创建时间字段才能统计

        return mapOf(
            "total" to total,
            "open" to open,
            "closed" to closed,
            "pending" to pending,
            "todayCreated" to todayCreated,
            "thisMonthCreated" to thisMonthCreated
        )
    }

    /** 批量删除活动。 */
    override fun batchDelete(ids: List<Int>): Map<String, Any> {
        val failedIds = mutableListOf<Int>()
        val failedReasons = mutableMapOf<String, String>()
        var successCount = 0

        ids.forEach { id ->
            try {
                if (activitiesRepository.existsById(id)) {
                    activitiesRepository.deleteById(id)
                    successCount++
                } else {
                    failedIds.add(id)
                    failedReasons[id.toString()] = "活动不存在或已被删除"
                }
            } catch (e: Exception) {
                failedIds.add(id)
                failedReasons[id.toString()] = e.message ?: "删除失败"
            }
        }

        return mapOf(
            "successCount" to successCount,
            "failedCount" to failedIds.size,
            "failedIds" to failedIds,
            "failedReasons" to failedReasons
        )
    }

    /** 复制活动。 */
    override fun duplicateActivity(id: Int, newUrl: String, newName: String?): Activities {
        val original = activitiesRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("活动不存在") }

        if (activitiesRepository.existsByUrl(newUrl)) {
            throw IllegalArgumentException("URL 已存在")
        }

        val duplicate = Activities(
            id = null,
            url = newUrl,
            masterTitle = original.masterTitle,
            subtitle = original.subtitle,
            name = newName ?: "${original.name}（副本）",
            startTime = original.startTime,
            endTime = original.endTime,
            bannerTags = original.bannerTags,
            bannerUrl = original.bannerUrl,
            isAnimation = original.isAnimation,
            isTopNavigationBar = original.isTopNavigationBar,
            icp = original.icp,
            technicalSupport = original.technicalSupport,
            isOpen = false,
            mealList = original.mealList.toMutableList(),
            hostedList = original.hostedList.toMutableList(),
            inspectionPoints = original.inspectionPoints.toMutableList(),
            overviewOfTheCityAndCounty = original.overviewOfTheCityAndCounty.toMutableList()
        )

        return activitiesRepository.save(duplicate)
    }

    /** 维护 JPA 双向关系中的子级反向引用。 */
    private fun bindChildren(activity: Activities) {
        activity.schedules.forEach { schedule ->
            schedule.activity = activity
            schedule.inspectionTeamItem.forEach { it.activity = activity }
        }
        activity.carList.forEach { it.activity = activity }
        activity.personList.forEach { it.activity = activity }
        activity.imageList.forEach { it.activity = activity }
        activity.promptServiceList.forEach { it.activity = activity }
        activity.inspectionTeamItemList.forEach { it.activity = activity }
    }

    private fun copyEditableFields(source: Activities, target: Activities) {
        target.url = source.url
        target.masterTitle = source.masterTitle
        target.subtitle = source.subtitle
        target.name = source.name
        target.startTime = source.startTime
        target.endTime = source.endTime
        target.bannerTags = source.bannerTags
        target.bannerUrl = source.bannerUrl
        target.isAnimation = source.isAnimation
        target.isTopNavigationBar = source.isTopNavigationBar
        target.icp = source.icp
        target.technicalSupport = source.technicalSupport
        target.isOpen = source.isOpen

        if (source.mealList.isNotEmpty()) target.mealList = source.mealList
        if (source.hostedList.isNotEmpty()) target.hostedList = source.hostedList
        if (source.inspectionPoints.isNotEmpty()) target.inspectionPoints = source.inspectionPoints
        if (source.overviewOfTheCityAndCounty.isNotEmpty()) {
            target.overviewOfTheCityAndCounty = source.overviewOfTheCityAndCounty
        }
    }
}
