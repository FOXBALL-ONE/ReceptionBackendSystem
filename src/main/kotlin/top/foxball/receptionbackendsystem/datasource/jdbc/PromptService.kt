package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * 提示服务配置。
 *
 * 用于维护接待页面中的工作人员提示、注意事项、天气提示和出席说明。
 */
@Table(name = "prompt_service")
@Entity
data class PromptService (
    /** 提示服务配置主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除提示服务配置。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 工作人员分组列表。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "staff_list", columnDefinition = "jsonb")
    var staffList: MutableList<StaffItem> = mutableListOf(),

    /** 注意事项列表。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "note_list", columnDefinition = "jsonb")
    var noteList: MutableList<NoteItem> = mutableListOf(),

    /** 天气提示列表。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weather_list", columnDefinition = "jsonb")
    var weatherList: MutableList<WeatherItem> = mutableListOf(),

    /** 是否开启出席说明。 */
    @Column(name = "attendance_instructions_mode")
    var attendanceInstructionsMode: Boolean? = null,

    /** 出席说明标题。 */
    @Column(name = "attendance_instructions_title")
    var attendanceInstructionsTitle: String? = null,

    /** 出席说明内容。 */
    @Column(name = "attendance_instructions_content", columnDefinition = "text")
    var attendanceInstructionsContent: String? = null,

)

/**
 * 工作人员分组。
 */
data class StaffItem(
    /** 分组名称。 */
    var name: String? = null,

    /** 分组颜色标签标识。 */
    var colorTag: String? = null,

    /** 分组下的人员列表。 */
    var groupList: MutableList<OneStaff> = mutableListOf(),
)

/**
 * 单个工作人员。
 */
data class OneStaff(
    /** 工作人员姓名。 */
    var name: String? = null,

    /** 工作人员职务或职责。 */
    var duty: String? = null,

    /** 工作人员联系电话。 */
    var phone: String? = null,
)

/**
 * 注意事项。
 */
data class NoteItem(
    /** 注意事项标题。 */
    var title: String? = null,

    /** 注意事项内容。 */
    var content: String? = null,

    /** 注意事项颜色标签标识。 */
    var colorTag: String? = null,
)

/**
 * 天气提示。
 */
data class WeatherItem(
    /** 天气提示对应的日期或时间。 */
    var time: LocalDateTime? = null,

    /** 城市。 */
    var city: String? = null,

    /** 温度描述。 */
    var temperature: String? = null,

    /** 天气现象描述。 */
    var weatherDescriptor: String? = null,
)
