package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.json.bind.annotation.JsonbTransient
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * 活动人员实体。
 *
 * 保存活动关联人员的姓名、单位和昵称等基础信息。
 */
@Table(name = "person")
@Entity
data class Person(
    /** 人员主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 所属活动，删除活动时由数据库级联删除人员记录。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 人员姓名。 */
    @Column(name = "name")
    var name: String? = null,

    /** 所属单位。 */
    @Column(name = "unit")
    var unit: String? = null,

    /** 昵称或展示名。 */
    @Column(name = "nick_name")
    var nickName: String? = null,

    /** 人员颜色分组。 */
    @JsonbTransient
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_tag_id")
    var colorTag: ColorTag? = null,

    @Column(name = "inspection_team_item_id")
    var inspectionTeamItemId: Long? = null,
)
