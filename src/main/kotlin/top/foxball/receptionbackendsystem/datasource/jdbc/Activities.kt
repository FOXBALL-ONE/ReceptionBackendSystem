package top.foxball.receptionbackendsystem.datasource.jdbc

import jakarta.persistence.*
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

    /** 活动人员列表，删除活动时级联删除对应人员。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var personList: MutableList<Person> = mutableListOf(),

    /** 活动图片列表，删除活动时级联删除对应图片元数据。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var imageList: MutableList<Image> = mutableListOf(),

    /** 活动提示服务配置列表，删除活动时级联删除对应配置。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var promptServiceList: MutableList<PromptService> = mutableListOf(),

    /** 活动颜色标签列表，删除活动时级联删除对应颜色标签。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var colorTagList: MutableList<ColorTag> = mutableListOf(),

    /** 活动考察组安排列表，删除活动时级联删除对应考察组。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var inspectionTeamItemList: MutableList<InspectionTeamItem> = mutableListOf(),

    /** 用餐安排列表，删除活动时级联删除对应用餐记录。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var mealList: MutableList<Meal> = mutableListOf(),

    /** 住宿安排列表，删除活动时级联删除对应住宿记录。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var hostedList: MutableList<Lodging> = mutableListOf(),

    /** 考察点列表，删除活动时级联删除对应考察点记录。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var inspectionPoints: MutableList<InspectionPoint> = mutableListOf(),

    /** 市县概况列表，删除活动时级联删除对应概况记录。 */
    @OneToMany(mappedBy = "activity", cascade = [CascadeType.ALL], orphanRemoval = true)
    var overviewOfTheCityAndCounty: MutableList<OverviewOfTheCityAndCounty> = mutableListOf(),
)
