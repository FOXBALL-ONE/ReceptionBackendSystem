package top.foxball.receptionbackendsystem.controller.request

import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.NoteItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.StaffItem
import top.foxball.receptionbackendsystem.datasource.jdbc.WeatherItem
import java.time.LocalDateTime

/** 整型 id 请求体 */
data class IntIdRequest(
    val id: Int? = null,
)

/** 长整型 id 请求体 */
data class LongIdRequest(
    val id: Long? = null,
)

/** 整型 id 批量请求体 */
data class IntIdsRequest(
    val ids: List<Int> = emptyList(),
)

/** 长整型 id 批量请求体 */
data class LongIdsRequest(
    val ids: List<Long> = emptyList(),
)

/** 活动 id 请求体 */
data class ActivityIdRequest(
    val activityId: Long? = null,
    val type: String? = null,
)

/** 活动开放状态切换请求体 */
data class ActivityOpenRequest(
    val id: Long? = null,
    val isOpen: Boolean? = null,
)

/** 活动基础信息保存请求体 */
data class ActivityBasicSaveRequest(
    val id: Long? = null,
    val url: String? = null,
    val masterTitle: String? = null,
    val subtitle: String? = null,
    val name: String? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val bannerTags: String? = null,
    val bannerUrl: String? = null,
    val isAnimation: Boolean? = null,
    val isTopNavigationBar: Boolean? = null,
    val icp: String? = null,
    val technicalSupport: String? = null,
    val isOpen: Boolean? = null,
) {
    /** 映射为活动实体。 */
    fun toEntity(): Activities =
        Activities(
            id = id,
            url = url,
            masterTitle = masterTitle,
            subtitle = subtitle,
            name = name,
            startTime = startTime,
            endTime = endTime,
            bannerTags = bannerTags,
            bannerUrl = bannerUrl,
            isAnimation = isAnimation,
            isTopNavigationBar = isTopNavigationBar,
            icp = icp,
            technicalSupport = technicalSupport,
            isOpen = isOpen,
        )
}

/** url 请求体 */
data class UrlRequest(
    val url: String? = null,
)

/** 名称请求体 */
data class NameRequest(
    val name: String? = null,
)

/** 活动名称请求体 */
data class ActivityNameRequest(
    val activityId: Long? = null,
    val name: String? = null,
    val type: String? = null,
)

/** 颜色标签保存请求体 */
data class ColorTagSaveRequest(
    val activityId: Long? = null,
    val id: Int? = null,
    val name: String? = null,
    val color: String? = null,
    val type: String? = null,
) {
    /** 映射为颜色标签实体。 */
    fun toEntity(): ColorTag =
        ColorTag(
            id = id,
            name = name,
            color = color,
            type = type,
        )
}

/** 活动单位请求体 */
data class ActivityUnitRequest(
    val activityId: Long? = null,
    val unit: String? = null,
)

/** 活动车号请求体 */
data class ActivityCarNumberRequest(
    val activityId: Long? = null,
    val carNumber: Long? = null,
)

/** 日程标签请求体 */
data class ScheduleTagsRequest(
    val scheduleTags: String? = null,
)

/** 日程保存请求体 */
data class ScheduleSaveRequest(
    val activityId: Long? = null,
    val schedules: List<Schedule> = emptyList(),
)

/**
 * 考察组整体保存请求：考察组身份 + 其各天行程。
 *
 * 考察组 id 跨天唯一；每条行程以 [InspectionTeamItineraryDto.scheduleId] 指向已保存的日程天。
 */
data class InspectionTeamSaveRequest(
    val activityId: Long? = null,
    val items: List<InspectionTeamItemDto> = emptyList(),
)

/** 考察组身份 DTO：id 跨天唯一，附带各天行程。 */
data class InspectionTeamItemDto(
    val id: Long? = null,
    val name: String? = null,
    val itineraries: List<InspectionTeamItineraryDto> = emptyList(),
)

data class InspectionTeamItineraryDto(
    val id: Long? = null,
    /** 所属日程天 id，行程按天独立。 */
    val scheduleId: Long? = null,
    val routeUrl: String? = null,
    val scheduleUrl: String? = null,
    val routeNode: List<String> = emptyList(),
    val eventArrangements: List<EventArrangementsItem> = emptyList(),
)

/** 人员保存请求体 */
data class PersonSaveRequest(
    val activityId: Long? = null,
    val persons: List<Person> = emptyList(),
)

/** 餐饮保存请求体 */
data class MealSaveRequest(
    val activityId: Long? = null,
    val meals: List<Meal> = emptyList(),
)

/** 住宿保存请求体 */
data class LodgingSaveRequest(
    val activityId: Long? = null,
    val colorTags: List<ColorTag> = emptyList(),
    val lodgings: List<Lodging> = emptyList(),
)

/** 考察点位保存请求体 */
data class InspectionPointSaveRequest(
    val activityId: Long? = null,
    val inspectionPoints: List<InspectionPoint> = emptyList(),
)

/** 车辆保存请求体 */
data class CarSaveRequest(
    val activityId: Long? = null,
    val cars: List<Car> = emptyList(),
)

/** 提示服务保存请求体 */
data class PromptServiceSaveRequest(
    val activityId: Long? = null,
    val id: Int? = null,
    val staffList: List<StaffItem> = emptyList(),
    val noteList: List<NoteItem> = emptyList(),
    val weatherList: List<WeatherItem> = emptyList(),
    val attendanceInstructionsMode: Boolean? = null,
    val attendanceInstructionsTitle: String? = null,
    val attendanceInstructionsContent: String? = null,
) {
    /** 映射为提示服务实体。 */
    fun toEntity(): PromptService =
        PromptService(
            id = id,
            staffList = staffList.toMutableList(),
            noteList = noteList.toMutableList(),
            weatherList = weatherList.toMutableList(),
            attendanceInstructionsMode = attendanceInstructionsMode,
            attendanceInstructionsTitle = attendanceInstructionsTitle,
            attendanceInstructionsContent = attendanceInstructionsContent,
        )
}

/** 实体批量保存请求体 */
data class EntityBatchRequest<T>(
    val activityId: Long? = null,
    val items: List<T> = emptyList(),
)
