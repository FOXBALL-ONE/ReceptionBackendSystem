package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * 活动日程实体（天注册表）。
 *
 * 保存活动下“每天”的标签（scheduleTags），作为各天顺序的权威来源。
 * 考察组行程不再挂在本实体下，而是按 [InspectionTeamItinerary] 关联到具体某天。
 */
@Table(name = "schedule")
@Entity
data class Schedule(
    /** 日程主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 所属活动，删除活动时由数据库级联删除日程记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 日程标签文案，多个标签由业务层约定格式。 */
    @Column(name = "schedule_tags")
    var scheduleTags: String? = null,
)
