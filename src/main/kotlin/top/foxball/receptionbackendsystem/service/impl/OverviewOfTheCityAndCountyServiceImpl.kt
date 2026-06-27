package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCountyRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.OverviewOfTheCityAndCountyService

/**
 * 市县概况服务实现，操作 [OverviewOfTheCityAndCounty] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的市县概况集合：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的概况及其描述段落。
 */
@Service
class OverviewOfTheCityAndCountyServiceImpl(
    private val overviewRepository: OverviewOfTheCityAndCountyRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<OverviewOfTheCityAndCounty, Int>(overviewRepository), OverviewOfTheCityAndCountyService {
    /** 按活动查询其下全部市县概况。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<OverviewOfTheCityAndCounty> =
        overviewRepository.findByActivityId(activityId)

    /**
     * 整体覆盖保存某活动的市县概况集合。
     *
     * 步骤：1) 加载活动并按 id 建立既有概况索引；
     * 2) 逐项归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与标题、头图、描述段落；
     *    描述段落 [OverviewOfTheCityAndCounty.description] 按深拷贝重建；
     * 3) 清空 [Activities.overviewOfTheCityAndCounty] 后整体替换为归一化结果并保存活动。
     */
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
