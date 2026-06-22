package top.foxball.receptionbackendsystem.controller.request

import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule

/**
 * 无查询条件请求体。
 */
class EmptyRequest

/**
 * Int 类型主键请求体。
 */
data class IntIdRequest(
    val id: Int = 0,
)

/**
 * Long 类型主键请求体。
 */
data class LongIdRequest(
    val id: Long = 0,
)

/**
 * 活动访问地址请求体。
 */
data class UrlRequest(
    val url: String = "",
)

/**
 * 名称请求体。
 */
data class NameRequest(
    val name: String = "",
)

/**
 * 活动主键请求体。
 */
data class ActivityIdRequest(
    val activityId: Int = 0,
)

/**
 * 活动车号查询请求体。
 */
data class ActivityCarNumberRequest(
    val activityId: Int = 0,
    val carNumber: Long = 0,
)

/**
 * 活动单位查询请求体。
 */
data class ActivityUnitRequest(
    val activityId: Int = 0,
    val unit: String = "",
)

/**
 * 日程标签查询请求体。
 */
data class ScheduleTagsRequest(
    val scheduleTags: String = "",
)

/**
 * 图片对象键查询请求体。
 */
data class ObjectKeyRequest(
    val objectKey: String = "",
)

/**
 * 图片 SHA-256 查询请求体。
 */
data class Sha256Request(
    val sha256: String = "",
)

/**
 * 图片用途类型查询请求体。
 */
data class UsageTypeRequest(
    val usageType: String = "",
)

/**
 * 图片上传元信息请求体。
 */
data class UploadImageMetadataRequest(
    val usageType: String? = null,
    val uploadedBy: String? = null,
)

/**
 * 活动更新请求体。
 */
data class UpdateActivityRequest(
    val id: Int = 0,
    val activity: Activities = Activities(),
)

/**
 * 车辆创建请求体。
 */
data class CreateCarRequest(
    val activityId: Int = 0,
    val car: Car = Car(),
)

/**
 * 车辆更新请求体。
 */
data class UpdateCarRequest(
    val id: Int = 0,
    val car: Car = Car(),
)

/**
 * 颜色标签更新请求体。
 */
data class UpdateColorTagRequest(
    val id: Int = 0,
    val colorTag: ColorTag = ColorTag(),
)

/**
 * 图片元数据更新请求体。
 */
data class UpdateImageRequest(
    val id: Long = 0,
    val image: Image = Image(),
)

/**
 * 考察组安排更新请求体。
 */
data class UpdateInspectionTeamItemRequest(
    val id: Long = 0,
    val inspectionTeamItem: InspectionTeamItem = InspectionTeamItem(),
)

/**
 * 人员创建请求体。
 */
data class CreatePersonRequest(
    val activityId: Int = 0,
    val person: Person = Person(),
)

/**
 * 人员更新请求体。
 */
data class UpdatePersonRequest(
    val id: Int = 0,
    val person: Person = Person(),
)

/**
 * 提示服务配置更新请求体。
 */
data class UpdatePromptServiceRequest(
    val id: Int = 0,
    val promptService: PromptService = PromptService(),
)

/**
 * 日程创建请求体。
 */
data class CreateScheduleRequest(
    val activityId: Int = 0,
    val schedule: Schedule = Schedule(),
)

/**
 * 日程更新请求体。
 */
data class UpdateScheduleRequest(
    val id: Long = 0,
    val schedule: Schedule = Schedule(),
)

/**
 * 活动搜索请求体。
 */
data class SearchActivitiesRequest(
    val keyword: String? = null,
    val status: String? = null,
)

/**
 * 二维码生成请求体。
 */
data class QrcodeRequest(
    val id: Int = 0,
    val size: Int = 256,
)

/**
 * 批量删除请求体。
 */
data class BatchDeleteRequest(
    val ids: List<Int> = emptyList(),
)

/**
 * 活动复制请求体。
 */
data class DuplicateActivityRequest(
    val id: Int = 0,
    val newUrl: String = "",
    val newName: String? = null,
)

/**
 * 日程批量保存请求体。
 */
data class BatchSaveSchedulesRequest(
    val activityId: Int = 0,
    val schedules: List<Schedule> = emptyList(),
)

/**
 * 人员批量保存请求体。
 */
data class BatchSavePersonsRequest(
    val activityId: Int = 0,
    val persons: List<Person> = emptyList(),
)

/**
 * 车辆批量保存请求体。
 */
data class BatchSaveCarsRequest(
    val activityId: Int = 0,
    val cars: List<Car> = emptyList(),
)
