package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * 活动日程实体。
 *
 * 保存活动下的日程标签以及该日程关联的考察组安排。
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

    /** 考察组安排列表，随日程级联保存和删除。 */
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "schedule_id")
    @OrderColumn(name = "inspection_team_order")
    var inspectionTeamItem: MutableList<InspectionTeamItem> = mutableListOf(),
)
