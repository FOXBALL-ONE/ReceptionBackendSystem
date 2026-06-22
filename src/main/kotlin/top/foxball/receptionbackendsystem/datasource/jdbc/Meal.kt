package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * 用餐安排实体。
 *
 * 通过 activity_id 标记所属活动，便于按活动独立维护用餐安排。
 */
@Table(name = "meal")
@Entity
data class Meal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 餐食名称。 */
    @Column(name = "name")
    var name: String? = null,

    /** 餐食说明。 */
    @Column(name = "description")
    var description: String? = null,

    /** 用餐地点。 */
    @Column(name = "position")
    var position: String? = null,

    /** 用餐时间。 */
    @Column(name = "time")
    var time: LocalDateTime? = null,
)
