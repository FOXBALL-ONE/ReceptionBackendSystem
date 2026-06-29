package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPointRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.InspectionPointService

/**
 * 考察点服务实现，操作 [InspectionPoint] 实体。
 *
 * 所有写方法均通过基类 [AbstractReceptionService] 包装为单事务。
 * [saveByActivity] 以「按活动整体覆盖」语义重建该活动的考察点集合：清空既有集合后按请求重建，
 * 借助活动实体的 orphanRemoval 级联清理被移除的记录。
 */
@Service
class InspectionPointServiceImpl(
    private val inspectionPointRepository: InspectionPointRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<InspectionPoint, Int>(inspectionPointRepository), InspectionPointService {
    /** 按活动查询其下全部考察点。 */
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<InspectionPoint> =
        inspectionPointRepository.findByActivityId(activityId)

    /**
     * 整体覆盖保存某活动的考察点集合。
     *
     * 步骤：1) 加载活动并按 id 建立既有考察点索引；
     * 2) 逐项归一化：命中 id 复用既有实体做更新，否则新建，并回填活动引用与字段；
     * 3) 清空 [Activities.inspectionPoints] 后整体替换为归一化结果并保存活动。
     */
    @Transactional
    override fun saveByActivity(activityId: Long, inspectionPoints: List<InspectionPoint>): List<InspectionPoint> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingInspectionPointsById = activity.inspectionPoints.mapNotNull { inspectionPoint ->
            inspectionPoint.id?.let { it to inspectionPoint }
        }.toMap()

        val normalizedInspectionPoints = inspectionPoints.map { inspectionPoint ->
            val targetInspectionPoint = inspectionPoint.id?.let(existingInspectionPointsById::get) ?: InspectionPoint()
            targetInspectionPoint.activity = activity
            targetInspectionPoint.name = inspectionPoint.name
            targetInspectionPoint.imageURL = inspectionPoint.imageURL
            targetInspectionPoint.description = inspectionPoint.description
            targetInspectionPoint
        }

        activity.inspectionPoints.clear()
        activity.inspectionPoints.addAll(normalizedInspectionPoints)

        return activitiesRepository.saveAndFlush(activity).inspectionPoints
    }
}
