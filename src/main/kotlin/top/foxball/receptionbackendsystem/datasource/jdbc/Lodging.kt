package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.type.SqlTypes

/**
 * 住宿安排实体。
 *
 * 通过 activity_id 标记所属活动，颜色标签统一关联全局 ColorTag。
 */
@Table(name = "lodging")
@Entity
data class Lodging(
    /** 住宿记录主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除住宿记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 房间号。 */
    @Column(name = "room_number")
    var roomNumber: String? = null,

    /** 入住人员快照。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "person", columnDefinition = "jsonb")
    var person: Person? = null,

    /** 所属颜色标签，用于住宿分组的颜色标记。 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_tag_id")
    var colorTag: ColorTag? = null,
) {
    /**
     * 兼容旧的前端字段名，真实数据来源仍是全局 ColorTag。
     */
    @get:JsonProperty("hostedColorsTag")
    val hostedColorsTag: ColorTag?
        get() = colorTag
}
