package top.foxball.receptionbackendsystem.datasource.jdbc

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime

/**
 * 考察组身份实体。
 *
 * 代表活动下一个跨天共用的考察组身份（名称），供人员分组绑定使用。
 * 每天的具体行程（路线、事件安排、文件地址）存放在 [itineraries] 中，按天独立。
 */
@Entity
@Table(name = "inspection_team_item")
data class InspectionTeamItem(
    /** 考察组身份主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    /** 考察组名称，跨天共用同一身份。 */
    @Column(name = "name")
    var name: String? = null,

    /** 各天行程列表，删除考察组时级联删除对应行程。 */
    @OneToMany(mappedBy = "inspectionTeam", cascade = [CascadeType.ALL], orphanRemoval = true)
    var itineraries: MutableList<InspectionTeamItinerary> = mutableListOf(),
)

/**
 * 考察组某一天的行程安排。
 *
 * 每条记录对应一个考察组（[inspectionTeam]）在某一天（[schedule]）的路线节点、
 * 事件安排及路线图/日程文件地址。同一考察组在不同天有各自独立的行程。
 */
@Entity
@Table(name = "inspection_team_itinerary")
data class InspectionTeamItinerary(
    /** 行程主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_team_item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var inspectionTeam: InspectionTeamItem? = null,

    /** 所属日程天，行程按天独立。 */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var schedule: Schedule? = null,

    /** 当天路线文件或路线页面地址。 */
    @Column(name = "route_url")
    var routeUrl: String? = null,

    /** 当天日程文件或日程页面地址。 */
    @Column(name = "schedule_url")
    var scheduleUrl: String? = null,

    /** 当天路线节点列表，按 node_order 保持前端展示顺序。 */
    @ElementCollection
    @CollectionTable(
        name = "inspection_team_itinerary_route_node",
        joinColumns = [JoinColumn(name = "inspection_team_itinerary_id")]
    )
    @OrderColumn(name = "node_order")
    @Column(name = "route_node")
    var routeNode: MutableList<String> = mutableListOf(),

    /** 当天事件安排列表，按 event_order 保持前端展示顺序。 */
    @ElementCollection
    @CollectionTable(
        name = "inspection_team_itinerary_event_arrangements",
        joinColumns = [JoinColumn(name = "inspection_team_itinerary_id")]
    )
    @OrderColumn(name = "event_order")
    var eventArrangements: MutableList<EventArrangementsItem> = mutableListOf(),
)

/** 考察组事件安排值对象。 */
@Embeddable
data class EventArrangementsItem(
    /** 事件开始时间。 */
    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    /** 事件结束时间。 */
    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    /** 事件内容。 */
    @Column(name = "content")
    var content: String? = null,

    /** 事件地点。 */
    @Column(name = "location")
    var location: String? = null,
)
