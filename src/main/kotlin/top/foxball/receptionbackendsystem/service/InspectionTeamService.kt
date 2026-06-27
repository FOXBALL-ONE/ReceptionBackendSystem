package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.controller.request.InspectionTeamItemDto
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem

/**
 * 考察组业务服务契约。
 *
 * 为 [InspectionTeamItem] 提供按活动维度查询、按名称模糊匹配
 * 以及按活动整体覆盖保存考察组身份及其各天行程。
 */
interface InspectionTeamService : ActivityEntityService<InspectionTeamItem, Long> {
    /**
     * 整体保存活动下的考察组身份及其各天行程。
     *
     * 考察组按 id 匹配做更新/新增/删除；每条行程通过 [InspectionTeamItemDto] 中的
     * `itineraries[].scheduleId` 关联到本活动下已保存的日程天。
     */
    fun saveByActivity(activityId: Long, groups: List<InspectionTeamItemDto>): List<InspectionTeamItem>

    /** 按考察组名称模糊匹配考察组身份。 */
    fun findByNameContaining(name: String): List<InspectionTeamItem>
}
