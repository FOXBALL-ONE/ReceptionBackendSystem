package top.foxball.receptionbackendsystem.controller.request

import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.NoteItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.StaffItem
import top.foxball.receptionbackendsystem.datasource.jdbc.WeatherItem

data class IntIdRequest(
    val id: Int? = null,
)

data class LongIdRequest(
    val id: Long? = null,
)

data class IntIdsRequest(
    val ids: List<Int> = emptyList(),
)

data class LongIdsRequest(
    val ids: List<Long> = emptyList(),
)

data class ActivityIdRequest(
    val activityId: Long? = null,
)

data class UrlRequest(
    val url: String? = null,
)

data class NameRequest(
    val name: String? = null,
)

data class ActivityNameRequest(
    val activityId: Long? = null,
    val name: String? = null,
)

data class ActivityUnitRequest(
    val activityId: Long? = null,
    val unit: String? = null,
)

data class ActivityCarNumberRequest(
    val activityId: Long? = null,
    val carNumber: Long? = null,
)

data class ScheduleTagsRequest(
    val scheduleTags: String? = null,
)

data class ScheduleSaveRequest(
    val activityId: Long? = null,
    val schedules: List<Schedule> = emptyList(),
)

data class PersonSaveRequest(
    val activityId: Long? = null,
    val persons: List<Person> = emptyList(),
)

data class MealSaveRequest(
    val activityId: Long? = null,
    val meals: List<Meal> = emptyList(),
)

data class LodgingSaveRequest(
    val activityId: Long? = null,
    val colorTags: List<ColorTag> = emptyList(),
    val lodgings: List<Lodging> = emptyList(),
)

data class InspectionPointSaveRequest(
    val activityId: Long? = null,
    val inspectionPoints: List<InspectionPoint> = emptyList(),
)

data class CarSaveRequest(
    val activityId: Long? = null,
    val cars: List<Car> = emptyList(),
)

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

data class EntityBatchRequest<T>(
    val items: List<T> = emptyList(),
)
