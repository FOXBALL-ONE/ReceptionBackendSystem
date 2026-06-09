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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

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
    var name: String? = null,
    var colorTag: String? = null,
    var groupList: MutableList<OneStaff> = mutableListOf(),
)

/**
 * 单个工作人员。
 */
data class OneStaff(
    var name: String? = null,
    var duty: String? = null,
    var phone: String? = null,
)

/**
 * 注意事项。
 */
data class NoteItem(
    var title: String? = null,
    var content: String? = null,
    var colorTag: String? = null,
)

/**
 * 天气提示。
 */
data class WeatherItem(
    var time: LocalDateTime? = null,
    var city: String? = null,
    var temperature: String? = null,
    var weatherDescriptor: String? = null,
)
