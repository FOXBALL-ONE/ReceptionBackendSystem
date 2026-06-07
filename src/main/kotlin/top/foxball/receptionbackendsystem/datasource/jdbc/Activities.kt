package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "activities")
@Entity
data class Activities(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "url")
    var url: String? = null,

    @Column(name = "master_title")
    var masterTitle: String? = null,

    @Column(name = "subtitle")
    var subtitle: String? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    @Column(name = "banner_tags")
    var bannerTags: String? = null,

    @Column(name = "banner_url")
    var bannerUrl: String? = null,

    @Column(name = "is_animation")
    var isAnimation: Boolean? = null,

    @Column(name = "is_top_navigation_bar")
    var isTopNavigationBar: Boolean? = null,

    @Column(name = "icp")
    var icp: String? = null,

    @Column(name = "technical_support")
    var technicalSupport: String? = null,

    @Column(name = "is_open")
    var isOpen : Boolean? = null,

    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var schedules: MutableList<Schedule> = mutableListOf(),

    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var carList: MutableList<Car> = mutableListOf(),

)
