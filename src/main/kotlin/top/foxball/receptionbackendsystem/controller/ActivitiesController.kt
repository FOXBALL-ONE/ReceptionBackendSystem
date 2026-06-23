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

@RestController
@RequestMapping("/api/activities")
class ActivitiesController(
    private val activitiesService: ActivitiesService,
    private val builder: ResponseBuilder,
) : ControllerSupport(builder) {
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

    @PostMapping("/save-one")
    fun saveOne(@RequestBody entity: Activities): ResponseEntity<ApiResponse> {
        val activity = activitiesService.saveOne(entity)

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

    @PostMapping("/update-one")
    fun updateOne(@RequestBody entity: Activities): ResponseEntity<ApiResponse> {
        val activity = activitiesService.updateOne(entity)

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

    @PostMapping("/delete-one")
    fun deleteOne(@RequestBody request: LongIdRequest): ResponseEntity<ApiResponse> =
        deleteById(request.id, activitiesService)

    @PostMapping("/delete-batch")
    fun deleteBatch(@RequestBody request: LongIdsRequest): ResponseEntity<ApiResponse> =
        deleteByIds(request.ids, activitiesService)

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

    @PostMapping("/exists-by-url")
    fun existsByUrl(@RequestBody request: UrlRequest): ResponseEntity<ApiResponse> {
        val url = request.url?.takeIf { it.isNotBlank() } ?: return badRequest("url is required")
        return ok(activitiesService.existsByUrl(url))
    }

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
