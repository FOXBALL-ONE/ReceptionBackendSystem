package top.foxball.receptionbackendsystem.datasource

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "inspection_team_item")
data class InspectionTeamItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "route_url")
    var routeUrl: String? = null,

    @Column(name = "schedule_url")
    var scheduleUrl: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "inspection_team_route_node",
        joinColumns = [JoinColumn(name = "inspection_team_item_id")]
    )
    @OrderColumn(name = "node_order")
    @Column(name = "route_node")
    var routeNode: MutableList<String> = mutableListOf(),

    @ElementCollection
    @CollectionTable(
        name = "inspection_team_event_arrangements",
        joinColumns = [JoinColumn(name = "inspection_team_item_id")]
    )
    @OrderColumn(name = "event_order")
    var eventArrangements: MutableList<EventArrangementsItem> = mutableListOf(),
)

@Embeddable
data class EventArrangementsItem(
    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Column(name = "content")
    var content: String? = null,

    @Column(name = "location")
    var location: String? = null,
)
