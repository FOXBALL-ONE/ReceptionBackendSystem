package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.ScheduleRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ScheduleService
import java.util.IdentityHashMap

@Service
class ScheduleServiceImpl(
    private val scheduleRepository: ScheduleRepository,
    private val activitiesRepository: ActivitiesRepository,
) : AbstractReceptionService<Schedule, Long>(scheduleRepository), ScheduleService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Long): List<Schedule> =
        scheduleRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule> =
        scheduleRepository.findByScheduleTagsContaining(scheduleTags)

    @Transactional
    override fun saveByActivity(activityId: Long, schedules: List<Schedule>): List<Schedule> {
        val activity = activitiesRepository.findEntityById(activityId)
            ?: throw ResourceNotFoundException("activity not found")
        val existingSchedulesById = activity.schedules.mapNotNull { schedule ->
            schedule.id?.let { it to schedule }
        }.toMap()
        val existingTeamsById = activity.inspectionTeamItemList.mapNotNull { inspectionTeam ->
            inspectionTeam.id?.let { it to inspectionTeam }
        }.toMap()

        val normalizedSchedules = schedules.map { schedule ->
            val targetSchedule = schedule.id?.let(existingSchedulesById::get) ?: Schedule()
            targetSchedule.activity = activity
            targetSchedule.scheduleTags = schedule.scheduleTags
            targetSchedule.inspectionTeamItem.clear()
            targetSchedule.inspectionTeamItem.addAll(
                schedule.inspectionTeamItem.map { inspectionTeam ->
                    val targetInspectionTeam = inspectionTeam.id?.let(existingTeamsById::get) ?: InspectionTeamItem()
                    targetInspectionTeam.activity = activity
                    targetInspectionTeam.name = inspectionTeam.name
                    targetInspectionTeam.routeUrl = inspectionTeam.routeUrl
                    targetInspectionTeam.scheduleUrl = inspectionTeam.scheduleUrl
                    targetInspectionTeam.routeNode.clear()
                    targetInspectionTeam.routeNode.addAll(inspectionTeam.routeNode)
                    targetInspectionTeam.eventArrangements.clear()
                    targetInspectionTeam.eventArrangements.addAll(inspectionTeam.eventArrangements.map { it.copy() })
                    targetInspectionTeam
                },
            )
            targetSchedule
        }

        val normalizedScheduleSet = IdentityHashMap<Schedule, Boolean>().apply {
            normalizedSchedules.forEach { put(it, true) }
        }
        activity.schedules.toList().forEach { existingSchedule ->
            if (!normalizedScheduleSet.containsKey(existingSchedule)) {
                existingSchedule.inspectionTeamItem.clear()
            }
        }

        activity.schedules.clear()
        activity.inspectionTeamItemList.clear()
        activity.schedules.addAll(normalizedSchedules)
        activity.inspectionTeamItemList.addAll(normalizedSchedules.flatMap(Schedule::inspectionTeamItem).distinctByIdentity())

        return activitiesRepository.saveAndFlush(activity).schedules
    }

    private fun List<InspectionTeamItem>.distinctByIdentity(): List<InspectionTeamItem> {
        val seen = IdentityHashMap<InspectionTeamItem, Boolean>()
        return filter { seen.put(it, true) == null }
    }
}
