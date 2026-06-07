package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 考察组安排实体。
 *
 * 保存单个考察组的名称、路线、日程附件地址以及事件安排。
 */
@Entity
@Table(name = "inspection_team_item")
data class InspectionTeamItem(
    /** 考察组安排主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    /** 考察组名称。 */
    @Column(name = "name")
    var name: String? = null,

    /** 路线文件或路线页面地址。 */
    @Column(name = "route_url")
    var routeUrl: String? = null,

    /** 日程文件或日程页面地址。 */
    @Column(name = "schedule_url")
    var scheduleUrl: String? = null,

    /** 路线节点列表，按 node_order 保持前端展示顺序。 */
    @ElementCollection
    @CollectionTable(
        name = "inspection_team_route_node",
        joinColumns = [JoinColumn(name = "inspection_team_item_id")]
    )
    @OrderColumn(name = "node_order")
    @Column(name = "route_node")
    var routeNode: MutableList<String> = mutableListOf(),

    /** 事件安排列表，按 event_order 保持前端展示顺序。 */
    @ElementCollection
    @CollectionTable(
        name = "inspection_team_event_arrangements",
        joinColumns = [JoinColumn(name = "inspection_team_item_id")]
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
