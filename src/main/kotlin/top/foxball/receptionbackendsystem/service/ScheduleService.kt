package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

/**
 * 活动日程业务服务。
 */
interface ScheduleService {
    /** 查询全部日程。 */
    fun findAll(): List<Schedule>

    /** 根据日程主键查询日程，不存在时抛出业务异常。 */
    fun findById(id: Long): Schedule

    /** 查询指定活动下的全部日程。 */
    fun findByActivityId(activityId: Int): List<Schedule>

    /** 根据日程标签模糊查询日程。 */
    fun findByScheduleTagsContaining(scheduleTags: String): List<Schedule>

    /** 在指定活动下创建日程。 */
    fun create(activityId: Int, schedule: Schedule): Schedule

    /** 更新日程信息。 */
    fun update(id: Long, schedule: Schedule): Schedule

    /** 删除指定日程。 */
    fun delete(id: Long)
}
