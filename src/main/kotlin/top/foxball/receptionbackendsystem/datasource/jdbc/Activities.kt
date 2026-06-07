package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * 活动配置实体。
 *
 * 对应前台接待活动的基础信息、页面展示配置，以及活动下属的日程、车辆和扩展展示数据。
 */
@Table(name = "activities")
@Entity
data class Activities(
    /** 活动主键。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int? = null,

    /** 活动访问地址或路由地址。 */
    @Column(name = "url")
    var url: String? = null,

    /** 页面主标题。 */
    @Column(name = "master_title")
    var masterTitle: String? = null,

    /** 页面副标题。 */
    @Column(name = "subtitle")
    var subtitle: String? = null,

    /** 活动名称。 */
    @Column(name = "name")
    var name: String? = null,

    /** 活动开始时间。 */
    @Column(name = "start_time")
    var startTime: LocalDateTime? = null,

    /** 活动结束时间。 */
    @Column(name = "end_time")
    var endTime: LocalDateTime? = null,

    /** Banner 标签文案，多个标签由业务层约定格式。 */
    @Column(name = "banner_tags")
    var bannerTags: String? = null,

    /** Banner 图片地址。 */
    @Column(name = "banner_url")
    var bannerUrl: String? = null,

    /** 是否启用页面动画效果。 */
    @Column(name = "is_animation")
    var isAnimation: Boolean? = null,

    /** 是否展示顶部导航栏。 */
    @Column(name = "is_top_navigation_bar")
    var isTopNavigationBar: Boolean? = null,

    /** 页面备案号。 */
    @Column(name = "icp")
    var icp: String? = null,

    /** 技术支持说明。 */
    @Column(name = "technical_support")
    var technicalSupport: String? = null,

    /** 活动是否对外开放。 */
    @Column(name = "is_open")
    var isOpen: Boolean? = null,

    /** 活动日程列表，删除活动时级联删除对应日程。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var schedules: MutableList<Schedule> = mutableListOf(),

    /** 活动车辆列表，删除活动时级联删除对应车辆。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var carList: MutableList<Car> = mutableListOf(),

    /** 用餐安排，使用 jsonb 保存前端展示所需的结构化数据。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meal_list", columnDefinition = "jsonb")
    var mealList: MutableList<MealItem> = mutableListOf(),

    /** 住宿安排，使用 jsonb 保存房间、人员和颜色标签。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hosted_list", columnDefinition = "jsonb")
    var hostedList: MutableList<HostedItem> = mutableListOf(),

    /** 考察点信息，使用 jsonb 保存图片和说明。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inspection_points", columnDefinition = "jsonb")
    var inspectionPoints: MutableList<InspectionPointItem> = mutableListOf(),

    /** 市县概况内容，使用 jsonb 保存多段图文介绍。 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "overview_of_the_city_and_county", columnDefinition = "jsonb")
    var overviewOfTheCityAndCounty: MutableList<OverviewOfTheCityAndCountyItem> = mutableListOf(),
)


/** 市县概况条目。 */
data class OverviewOfTheCityAndCountyItem(
    /** 概况标题。 */
    var title: String? = null,

    /** 顶部图片地址。 */
    var topImageUrl: String? = null,

    /** 概况正文段落。 */
    var description: MutableList<ParagraphContentItem> = mutableListOf(),
)

/** 图文段落内容。 */
data class ParagraphContentItem(
    /** 段落标题。 */
    var title: String? = null,

    /** 段落正文。 */
    var content: String? = null,
)

/** 用餐安排条目。 */
data class MealItem(
    /** 餐食名称。 */
    var name: String? = null,

    /** 餐食说明。 */
    var description: String? = null,

    /** 用餐地点。 */
    var position: String? = null,

    /** 用餐时间。 */
    var time: LocalDateTime? = null,
)

/** 住宿安排条目。 */
data class HostedItem(
    /** 房间号。 */
    var roomNumber: String? = null,

    /** 入住人员。 */
    var person: Person? = null,

    /** 住宿颜色标签。 */
    var hostedColorsTag: HostedColorsTag? = null,
)

/** 住宿颜色标签。 */
data class HostedColorsTag(
    /** 标签名称。 */
    var name: String? = null,

    /** 标签颜色值。 */
    var color: String? = null,
)

/** 考察点条目。 */
data class InspectionPointItem(
    /** 考察点图片地址。 */
    var imageURL: String? = null,

    /** 考察点说明。 */
    var description: String? = null,
)
