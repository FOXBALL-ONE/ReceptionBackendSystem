package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import top.foxball.receptionbackendsystem.datasource.InspectionTeamItem

@Table(name = "schedule")
@Entity
data class Schedule (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    var activity: Activities? = null,

    @Column(name = "schedule_tags")
    var scheduleTags : String? = null,


    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "schedule_id")
    @OrderColumn(name = "inspection_team_order")
    var inspectionTeamItem: MutableList<InspectionTeamItem> = mutableListOf(),
)




