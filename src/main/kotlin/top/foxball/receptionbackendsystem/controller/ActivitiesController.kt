package top.foxball.receptionbackendsystem.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import top.foxball.receptionbackendsystem.controller.request.*
import top.foxball.receptionbackendsystem.datasource.jdbc.*
import top.foxball.receptionbackendsystem.service.ActivitiesService
import top.foxball.receptionbackendsystem.shared.ResponseBuilder
import java.time.LocalDateTime
import top.foxball.receptionbackendsystem.shared.Response as ApiResponse

/**
 * 活动控制器，挂在 /api/activities 下。
 *
 * 管理活动基础配置与开放状态，提供单条/批量保存与更新、删除、列表、按 id/活动 id/ url 查询、存在性校验等端点。
 */
@RestController
@RequestMapping("/api/activities")
class ActivitiesController(
    private val activitiesService: ActivitiesService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
    /** 更新活动对外开关状态。 */
    @PostMapping("/update-is-open")
    fun updateIsOpen(@RequestBody request: ActivityOpenRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val isOpen = request.isOpen ?: return badRequest("isOpen is required")
        val activity = activitiesService.updateIsOpen(id, isOpen)

        data class Response(
            val id: Long?,
            val isOpen: Boolean?,
        )

        return builder.ok()
            .data(Response(id = activity.id, isOpen = activity.isOpen))
            .build()
    }

    /** 新增活动基础信息并返回。 */
    @PostMapping("/save-one")
    fun saveOne(@RequestBody request: ActivityBasicSaveRequest): ResponseEntity<ApiResponse> {
        val activity = activitiesService.saveBasic(request.toEntity())

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = Response(
            id = activity.id,
            url = activity.url,
            masterTitle = activity.masterTitle,
            subtitle = activity.subtitle,
            name = activity.name,
            startTime = activity.startTime,
            endTime = activity.endTime,
            bannerTags = activity.bannerTags,
            bannerUrl = activity.bannerUrl,
            isAnimation = activity.isAnimation,
            isTopNavigationBar = activity.isTopNavigationBar,
            icp = activity.icp,
            technicalSupport = activity.technicalSupport,
            isOpen = activity.isOpen,
            schedules = activity.schedules,
            carList = activity.carList,
            personList = activity.personList,
            imageList = activity.imageList,
            promptServiceList = activity.promptServiceList,
            colorTagList = activity.colorTagList,
            inspectionTeamItemList = activity.inspectionTeamItemList,
            mealList = activity.mealList,
            hostedList = activity.hostedList,
            inspectionPoints = activity.inspectionPoints,
            overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
        )

        return builder.ok().data(rs).build()
    }

    /** 批量新增活动基础信息并返回。 */
    @PostMapping("/save-batch")
    fun saveBatch(@RequestBody request: EntityBatchRequest<Activities>): ResponseEntity<ApiResponse> {
        val activities = activitiesService.saveBatch(request.items)

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activities.map { activity ->
            Response(
                id = activity.id,
                url = activity.url,
                masterTitle = activity.masterTitle,
                subtitle = activity.subtitle,
                name = activity.name,
                startTime = activity.startTime,
                endTime = activity.endTime,
                bannerTags = activity.bannerTags,
                bannerUrl = activity.bannerUrl,
                isAnimation = activity.isAnimation,
                isTopNavigationBar = activity.isTopNavigationBar,
                icp = activity.icp,
                technicalSupport = activity.technicalSupport,
                isOpen = activity.isOpen,
                schedules = activity.schedules,
                carList = activity.carList,
                personList = activity.personList,
                imageList = activity.imageList,
                promptServiceList = activity.promptServiceList,
                colorTagList = activity.colorTagList,
                inspectionTeamItemList = activity.inspectionTeamItemList,
                mealList = activity.mealList,
                hostedList = activity.hostedList,
                inspectionPoints = activity.inspectionPoints,
                overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按 id 更新活动基础信息并返回。 */
    @PostMapping("/update-one")
    fun updateOne(@RequestBody request: ActivityBasicSaveRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val activity = activitiesService.updateBasic(id, request.toEntity())

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = Response(
            id = activity.id,
            url = activity.url,
            masterTitle = activity.masterTitle,
            subtitle = activity.subtitle,
            name = activity.name,
            startTime = activity.startTime,
            endTime = activity.endTime,
            bannerTags = activity.bannerTags,
            bannerUrl = activity.bannerUrl,
            isAnimation = activity.isAnimation,
            isTopNavigationBar = activity.isTopNavigationBar,
            icp = activity.icp,
            technicalSupport = activity.technicalSupport,
            isOpen = activity.isOpen,
            schedules = activity.schedules,
            carList = activity.carList,
            personList = activity.personList,
            imageList = activity.imageList,
            promptServiceList = activity.promptServiceList,
            colorTagList = activity.colorTagList,
            inspectionTeamItemList = activity.inspectionTeamItemList,
            mealList = activity.mealList,
            hostedList = activity.hostedList,
            inspectionPoints = activity.inspectionPoints,
            overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
        )

        return builder.ok().data(rs).build()
    }

    /** 批量更新活动基础信息并返回。 */
    @PostMapping("/update-batch")
    fun updateBatch(@RequestBody request: EntityBatchRequest<Activities>): ResponseEntity<ApiResponse> {
        val activities = activitiesService.updateBatch(request.items)

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activities.map { activity ->
            Response(
                id = activity.id,
                url = activity.url,
                masterTitle = activity.masterTitle,
                subtitle = activity.subtitle,
                name = activity.name,
                startTime = activity.startTime,
                endTime = activity.endTime,
                bannerTags = activity.bannerTags,
                bannerUrl = activity.bannerUrl,
                isAnimation = activity.isAnimation,
                isTopNavigationBar = activity.isTopNavigationBar,
                icp = activity.icp,
                technicalSupport = activity.technicalSupport,
                isOpen = activity.isOpen,
                schedules = activity.schedules,
                carList = activity.carList,
                personList = activity.personList,
                imageList = activity.imageList,
                promptServiceList = activity.promptServiceList,
                colorTagList = activity.colorTagList,
                inspectionTeamItemList = activity.inspectionTeamItemList,
                mealList = activity.mealList,
                hostedList = activity.hostedList,
                inspectionPoints = activity.inspectionPoints,
                overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按 id 删除单条活动。 */
    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, activitiesService)

    /** 按 id 列表批量删除活动。 */
    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, activitiesService)

    /** 按 id 查询单条活动，未找到返回 notFound。 */
    @PostMapping("/find-by-id")
    fun findById(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> {
        val id = request.id ?: return badRequest("id is required")
        val activity = activitiesService.findEntityById(id) ?: return notFound("entity not found")

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = Response(
            id = activity.id,
            url = activity.url,
            masterTitle = activity.masterTitle,
            subtitle = activity.subtitle,
            name = activity.name,
            startTime = activity.startTime,
            endTime = activity.endTime,
            bannerTags = activity.bannerTags,
            bannerUrl = activity.bannerUrl,
            isAnimation = activity.isAnimation,
            isTopNavigationBar = activity.isTopNavigationBar,
            icp = activity.icp,
            technicalSupport = activity.technicalSupport,
            isOpen = activity.isOpen,
            schedules = activity.schedules,
            carList = activity.carList,
            personList = activity.personList,
            imageList = activity.imageList,
            promptServiceList = activity.promptServiceList,
            colorTagList = activity.colorTagList,
            inspectionTeamItemList = activity.inspectionTeamItemList,
            mealList = activity.mealList,
            hostedList = activity.hostedList,
            inspectionPoints = activity.inspectionPoints,
            overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
        )

        return builder.ok().data(rs).build()
    }

    /** 查询所有活动。 */
    @PostMapping("/list")
    fun list(): ResponseEntity<ApiResponse> {
        val activities = activitiesService.findAllActivities()

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activities.map { activity ->
            Response(
                id = activity.id,
                url = activity.url,
                masterTitle = activity.masterTitle,
                subtitle = activity.subtitle,
                name = activity.name,
                startTime = activity.startTime,
                endTime = activity.endTime,
                bannerTags = activity.bannerTags,
                bannerUrl = activity.bannerUrl,
                isAnimation = activity.isAnimation,
                isTopNavigationBar = activity.isTopNavigationBar,
                icp = activity.icp,
                technicalSupport = activity.technicalSupport,
                isOpen = activity.isOpen,
                schedules = activity.schedules,
                carList = activity.carList,
                personList = activity.personList,
                imageList = activity.imageList,
                promptServiceList = activity.promptServiceList,
                colorTagList = activity.colorTagList,
                inspectionTeamItemList = activity.inspectionTeamItemList,
                mealList = activity.mealList,
                hostedList = activity.hostedList,
                inspectionPoints = activity.inspectionPoints,
                overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 查询所有活动（等价于 list）。 */
    @PostMapping("/find-all")
    fun findAll(): ResponseEntity<ApiResponse> {
        val activities = activitiesService.findAllActivities()

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activities.map { activity ->
            Response(
                id = activity.id,
                url = activity.url,
                masterTitle = activity.masterTitle,
                subtitle = activity.subtitle,
                name = activity.name,
                startTime = activity.startTime,
                endTime = activity.endTime,
                bannerTags = activity.bannerTags,
                bannerUrl = activity.bannerUrl,
                isAnimation = activity.isAnimation,
                isTopNavigationBar = activity.isTopNavigationBar,
                icp = activity.icp,
                technicalSupport = activity.technicalSupport,
                isOpen = activity.isOpen,
                schedules = activity.schedules,
                carList = activity.carList,
                personList = activity.personList,
                imageList = activity.imageList,
                promptServiceList = activity.promptServiceList,
                colorTagList = activity.colorTagList,
                inspectionTeamItemList = activity.inspectionTeamItemList,
                mealList = activity.mealList,
                hostedList = activity.hostedList,
                inspectionPoints = activity.inspectionPoints,
                overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按活动 id 查询活动详情。 */
    @PostMapping("/find-by-activity-id")
    fun findByActivityId(@RequestBody request: ActivityIdRequest): ResponseEntity<ApiResponse> {
        val activityId = request.activityId ?: return badRequest("activityId is required")
        val activity = activitiesService.findByActivityId(activityId)

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activity?.let {
            Response(
                id = it.id,
                url = it.url,
                masterTitle = it.masterTitle,
                subtitle = it.subtitle,
                name = it.name,
                startTime = it.startTime,
                endTime = it.endTime,
                bannerTags = it.bannerTags,
                bannerUrl = it.bannerUrl,
                isAnimation = it.isAnimation,
                isTopNavigationBar = it.isTopNavigationBar,
                icp = it.icp,
                technicalSupport = it.technicalSupport,
                isOpen = it.isOpen,
                schedules = it.schedules,
                carList = it.carList,
                personList = it.personList,
                imageList = it.imageList,
                promptServiceList = it.promptServiceList,
                colorTagList = it.colorTagList,
                inspectionTeamItemList = it.inspectionTeamItemList,
                mealList = it.mealList,
                hostedList = it.hostedList,
                inspectionPoints = it.inspectionPoints,
                overviewOfTheCityAndCounty = it.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 按 url 查询活动详情。 */
    @PostMapping("/find-by-url")
    fun findByUrl(@RequestBody request: UrlRequest): ResponseEntity<ApiResponse> {
        val url = request.url?.takeIf { it.isNotBlank() } ?: return badRequest("url is required")
        val activity = activitiesService.findByUrl(url)

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activity?.let {
            Response(
                id = it.id,
                url = it.url,
                masterTitle = it.masterTitle,
                subtitle = it.subtitle,
                name = it.name,
                startTime = it.startTime,
                endTime = it.endTime,
                bannerTags = it.bannerTags,
                bannerUrl = it.bannerUrl,
                isAnimation = it.isAnimation,
                isTopNavigationBar = it.isTopNavigationBar,
                icp = it.icp,
                technicalSupport = it.technicalSupport,
                isOpen = it.isOpen,
                schedules = it.schedules,
                carList = it.carList,
                personList = it.personList,
                imageList = it.imageList,
                promptServiceList = it.promptServiceList,
                colorTagList = it.colorTagList,
                inspectionTeamItemList = it.inspectionTeamItemList,
                mealList = it.mealList,
                hostedList = it.hostedList,
                inspectionPoints = it.inspectionPoints,
                overviewOfTheCityAndCounty = it.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }

    /** 校验指定 url 的活动是否已存在。 */
    @PostMapping("/exists-by-url")
    fun existsByUrl(@RequestBody request: UrlRequest): ResponseEntity<ApiResponse> {
        val url = request.url?.takeIf { it.isNotBlank() } ?: return badRequest("url is required")
        return ok(activitiesService.existsByUrl(url))
    }

    /** 查询所有已开放活动。 */
    @PostMapping("/find-open")
    fun findOpen(): ResponseEntity<ApiResponse> {
        val activities = activitiesService.findOpenActivities()

        data class Response(
            val id: Long?,
            val url: String?,
            val masterTitle: String?,
            val subtitle: String?,
            val name: String?,
            val startTime: LocalDateTime?,
            val endTime: LocalDateTime?,
            val bannerTags: String?,
            val bannerUrl: String?,
            val isAnimation: Boolean?,
            val isTopNavigationBar: Boolean?,
            val icp: String?,
            val technicalSupport: String?,
            val isOpen: Boolean?,
            val schedules: List<Schedule>,
            val carList: List<Car>,
            val personList: List<Person>,
            val imageList: List<Image>,
            val promptServiceList: List<PromptService>,
            val colorTagList: List<ColorTag>,
            val inspectionTeamItemList: List<InspectionTeamItem>,
            val mealList: List<Meal>,
            val hostedList: List<Lodging>,
            val inspectionPoints: List<InspectionPoint>,
            val overviewOfTheCityAndCounty: List<OverviewOfTheCityAndCounty>,
        )

        val rs = activities.map { activity ->
            Response(
                id = activity.id,
                url = activity.url,
                masterTitle = activity.masterTitle,
                subtitle = activity.subtitle,
                name = activity.name,
                startTime = activity.startTime,
                endTime = activity.endTime,
                bannerTags = activity.bannerTags,
                bannerUrl = activity.bannerUrl,
                isAnimation = activity.isAnimation,
                isTopNavigationBar = activity.isTopNavigationBar,
                icp = activity.icp,
                technicalSupport = activity.technicalSupport,
                isOpen = activity.isOpen,
                schedules = activity.schedules,
                carList = activity.carList,
                personList = activity.personList,
                imageList = activity.imageList,
                promptServiceList = activity.promptServiceList,
                colorTagList = activity.colorTagList,
                inspectionTeamItemList = activity.inspectionTeamItemList,
                mealList = activity.mealList,
                hostedList = activity.hostedList,
                inspectionPoints = activity.inspectionPoints,
                overviewOfTheCityAndCounty = activity.overviewOfTheCityAndCounty,
            )
        }

        return builder.ok().data(rs).build()
    }
}
