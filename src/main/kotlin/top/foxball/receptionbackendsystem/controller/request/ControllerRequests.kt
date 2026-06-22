package top.foxball.receptionbackendsystem.controller.request

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

data class EntityBatchRequest<T>(
    val items: List<T> = emptyList(),
)
