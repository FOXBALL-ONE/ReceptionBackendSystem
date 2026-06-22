package top.foxball.receptionbackendsystem.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Car
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.EventArrangementsItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionPoint
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Meal
import top.foxball.receptionbackendsystem.datasource.jdbc.NoteItem
import top.foxball.receptionbackendsystem.datasource.jdbc.OneStaff
import top.foxball.receptionbackendsystem.datasource.jdbc.OverviewOfTheCityAndCounty
import top.foxball.receptionbackendsystem.datasource.jdbc.ParagraphContentItem
import top.foxball.receptionbackendsystem.datasource.jdbc.PassengersOnBoardItem
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.datasource.jdbc.PromptService
import top.foxball.receptionbackendsystem.datasource.jdbc.Schedule
import top.foxball.receptionbackendsystem.datasource.jdbc.StaffItem
import top.foxball.receptionbackendsystem.datasource.jdbc.WeatherItem
import java.time.LocalDateTime

@Component
@Profile("dev", "test")
class MockDataInitializer(
    private val activitiesRepository: ActivitiesRepository,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun writeMockDataAfterApplicationReady() {
        if (activitiesRepository.existsByUrl(MOCK_ACTIVITY_URL)) {
            logger.info("Mock data already exists, skip initialization: url={}", MOCK_ACTIVITY_URL)
            return
        }

        val activity = createMockActivity()
        val savedActivity = activitiesRepository.saveAndFlush(activity)

        logger.info(
            "Mock data initialized: activityId={}, url={}, schedules={}, persons={}, cars={}",
            savedActivity.id,
            savedActivity.url,
            savedActivity.schedules.size,
            savedActivity.personList.size,
            savedActivity.carList.size,
        )
    }

    private fun createMockActivity(): Activities {
        val now = LocalDateTime.now().withSecond(0).withNano(0)
        val startTime = now.plusDays(3).withHour(9).withMinute(0)
        val endTime = startTime.plusDays(2).withHour(17).withMinute(30)

        val activity = Activities(
            url = MOCK_ACTIVITY_URL,
            masterTitle = "数字接待模拟活动",
            subtitle = "政务接待全流程演示",
            name = "2026年重点项目观摩接待",
            startTime = startTime,
            endTime = endTime,
            bannerTags = "重点项目,观摩调研,智慧接待",
            bannerUrl = "/images/images/95ba8f70-36e8-4d7d-8fd4-a7d516c7cbfe.png",
            isAnimation = true,
            isTopNavigationBar = true,
            icp = "京ICP备20260001号",
            technicalSupport = "技术支持：数字接待系统",
            isOpen = true,
        )

        val blueTag = ColorTag(activity = activity, name = "综合协调组", color = "#2563EB")
        val greenTag = ColorTag(activity = activity, name = "现场保障组", color = "#16A34A")
        val amberTag = ColorTag(activity = activity, name = "会务服务组", color = "#D97706")
        activity.colorTagList.addAll(listOf(blueTag, greenTag, amberTag))

        val projectTeam = InspectionTeamItem(
            activity = activity,
            name = "项目观摩组",
            routeUrl = "/mock/routes/project-team.pdf",
            scheduleUrl = "/mock/schedules/project-team.pdf",
            routeNode = mutableListOf("政务中心", "智能制造园", "新能源基地", "项目展厅"),
            eventArrangements = mutableListOf(
                EventArrangementsItem(
                    startTime = startTime.withHour(9).withMinute(30),
                    endTime = startTime.withHour(10).withMinute(30),
                    content = "听取重点项目建设情况汇报",
                    location = "政务中心第一会议室",
                ),
                EventArrangementsItem(
                    startTime = startTime.withHour(10).withMinute(45),
                    endTime = startTime.withHour(12).withMinute(0),
                    content = "观摩智能制造生产线",
                    location = "智能制造园",
                ),
            ),
        )

        val cityTeam = InspectionTeamItem(
            activity = activity,
            name = "城市更新组",
            routeUrl = "/mock/routes/city-team.pdf",
            scheduleUrl = "/mock/schedules/city-team.pdf",
            routeNode = mutableListOf("城市展馆", "更新示范街区", "公共服务中心"),
            eventArrangements = mutableListOf(
                EventArrangementsItem(
                    startTime = startTime.plusDays(1).withHour(9).withMinute(0),
                    endTime = startTime.plusDays(1).withHour(10).withMinute(30),
                    content = "参观城市更新成果展",
                    location = "城市展馆",
                ),
                EventArrangementsItem(
                    startTime = startTime.plusDays(1).withHour(10).withMinute(45),
                    endTime = startTime.plusDays(1).withHour(11).withMinute(45),
                    content = "调研便民服务建设",
                    location = "公共服务中心",
                ),
            ),
        )

        activity.inspectionTeamItemList.addAll(listOf(projectTeam, cityTeam))
        activity.schedules.addAll(
            listOf(
                Schedule(
                    activity = activity,
                    scheduleTags = "第一天,项目观摩,上午",
                    inspectionTeamItem = mutableListOf(projectTeam),
                ),
                Schedule(
                    activity = activity,
                    scheduleTags = "第二天,城市更新,上午",
                    inspectionTeamItem = mutableListOf(cityTeam),
                ),
                Schedule(
                    activity = activity,
                    scheduleTags = "第三天,总结交流,下午",
                ),
            ),
        )

        val personA = Person(activity = activity, name = "张明", unit = "省发展改革委", nickName = "张主任")
        val personB = Person(activity = activity, name = "李娜", unit = "省工业和信息化厅", nickName = "李处长")
        val personC = Person(activity = activity, name = "王强", unit = "市政府办公室", nickName = "王主任")
        val personD = Person(activity = activity, name = "赵琳", unit = "市招商服务中心", nickName = "赵经理")
        activity.personList.addAll(listOf(personA, personB, personC, personD))

        val personSnapshotA = personA.toJsonSnapshot()
        val personSnapshotB = personB.toJsonSnapshot()
        val personSnapshotC = personC.toJsonSnapshot()
        val personSnapshotD = personD.toJsonSnapshot()

        activity.carList.addAll(
            listOf(
                Car(
                    activity = activity,
                    carNumber = 1,
                    carPlate = "京A·M2026",
                    driver = "陈师傅",
                    driverNumber = "13800000001",
                    passengersOnBoardList = mutableListOf(
                        PassengersOnBoardItem(name = "随车联络员-刘洋", phone = "13900000001"),
                        PassengersOnBoardItem(name = "讲解员-周敏", phone = "13900000002"),
                    ),
                    passengersList = mutableListOf(personSnapshotA, personSnapshotB),
                ),
                Car(
                    activity = activity,
                    carNumber = 2,
                    carPlate = "京B·R2026",
                    driver = "孙师傅",
                    driverNumber = "13800000002",
                    passengersOnBoardList = mutableListOf(
                        PassengersOnBoardItem(name = "随车联络员-吴昊", phone = "13900000003"),
                    ),
                    passengersList = mutableListOf(personSnapshotC, personSnapshotD),
                ),
            ),
        )

        activity.imageList.addAll(
            listOf(
                Image(
                    activity = activity,
                    originalFilename = "mock-banner.jpg",
                    storedFilename = "mock-banner-2026.jpg",
                    extension = "jpg",
                    contentType = "image/jpeg",
                    fileSize = 1_245_600L,
                    width = 1920,
                    height = 1080,
                    sha256 = "0".repeat(64),
                    bucket = "mock-images",
                    objectKey = "mock/reception/banner.jpg",
                    storagePath = "./images/mock/reception/banner.jpg",
                    accessUrl = "/images/mock/reception/banner.jpg",
                    uploadedBy = "mock",
                    usageType = "banner",
                    isDeleted = false,
                    createdAt = now,
                    updatedAt = now,
                ),
                Image(
                    activity = activity,
                    originalFilename = "mock-project.jpg",
                    storedFilename = "mock-project-2026.jpg",
                    extension = "jpg",
                    contentType = "image/jpeg",
                    fileSize = 982_300L,
                    width = 1600,
                    height = 900,
                    sha256 = "1".repeat(64),
                    bucket = "mock-images",
                    objectKey = "mock/reception/project.jpg",
                    storagePath = "./images/mock/reception/project.jpg",
                    accessUrl = "/images/mock/reception/project.jpg",
                    uploadedBy = "mock",
                    usageType = "inspection",
                    isDeleted = false,
                    createdAt = now,
                    updatedAt = now,
                ),
            ),
        )

        activity.promptServiceList.add(
            PromptService(
                activity = activity,
                staffList = mutableListOf(
                    StaffItem(
                        name = "综合协调组",
                        colorTag = blueTag.color,
                        groupList = mutableListOf(
                            OneStaff(name = "刘洋", duty = "总协调", phone = "13900000001"),
                            OneStaff(name = "周敏", duty = "资料统筹", phone = "13900000002"),
                        ),
                    ),
                    StaffItem(
                        name = "现场保障组",
                        colorTag = greenTag.color,
                        groupList = mutableListOf(
                            OneStaff(name = "吴昊", duty = "现场保障", phone = "13900000003"),
                        ),
                    ),
                ),
                noteList = mutableListOf(
                    NoteItem(title = "集合提醒", content = "请各组提前15分钟到达指定集合点。", colorTag = amberTag.color),
                    NoteItem(title = "资料准备", content = "讲解材料、车辆牌号和住宿信息已同步至接待页面。", colorTag = blueTag.color),
                ),
                weatherList = mutableListOf(
                    WeatherItem(
                        time = startTime,
                        city = "北京市",
                        temperature = "20°C - 28°C",
                        weatherDescriptor = "晴",
                    ),
                    WeatherItem(
                        time = startTime.plusDays(1),
                        city = "北京市",
                        temperature = "19°C - 27°C",
                        weatherDescriptor = "多云",
                    ),
                ),
                attendanceInstructionsMode = true,
                attendanceInstructionsTitle = "出席说明",
                attendanceInstructionsContent = "请参会人员按日程安排参加活动，如需调整车辆或住宿，请联系综合协调组。",
            ),
        )

        activity.mealList.addAll(
            listOf(
                Meal(
                    activity = activity,
                    name = "欢迎午餐",
                    description = "本地特色工作餐",
                    position = "政务中心餐厅二层",
                    time = startTime.withHour(12).withMinute(15),
                ),
                Meal(
                    activity = activity,
                    name = "交流晚餐",
                    description = "圆桌交流餐",
                    position = "迎宾酒店三层宴会厅",
                    time = startTime.withHour(18).withMinute(30),
                ),
                Meal(
                    activity = activity,
                    name = "自助早餐",
                    description = "酒店自助早餐",
                    position = "迎宾酒店一层餐厅",
                    time = startTime.plusDays(1).withHour(7).withMinute(30),
                ),
            ),
        )

        activity.hostedList.addAll(
            listOf(
                Lodging(activity = activity, roomNumber = "1801", person = personSnapshotA, colorTag = blueTag),
                Lodging(activity = activity, roomNumber = "1802", person = personSnapshotB, colorTag = blueTag),
                Lodging(activity = activity, roomNumber = "1811", person = personSnapshotC, colorTag = greenTag),
                Lodging(activity = activity, roomNumber = "1812", person = personSnapshotD, colorTag = greenTag),
            ),
        )

        activity.inspectionPoints.addAll(
            listOf(
                InspectionPoint(
                    activity = activity,
                    imageURL = "/images/mock/reception/project.jpg",
                    description = "智能制造园聚焦高端装备和数字化产线，适合作为项目观摩核心点位。",
                ),
                InspectionPoint(
                    activity = activity,
                    imageURL = "/images/mock/reception/service-center.jpg",
                    description = "公共服务中心集中展示政务服务、民生服务和智慧城市应用。",
                ),
                InspectionPoint(
                    activity = activity,
                    imageURL = "/images/mock/reception/exhibition.jpg",
                    description = "城市展馆通过图文、视频和模型展示区域发展规划。",
                ),
            ),
        )

        activity.overviewOfTheCityAndCounty.add(
            OverviewOfTheCityAndCounty(
                activity = activity,
                title = "区域经济社会发展概况",
                topImageUrl = "/images/mock/reception/overview.jpg",
                description = mutableListOf(
                    ParagraphContentItem(
                        title = "基本情况",
                        content = "本区域交通区位便利，产业基础完善，具备承接重点项目和组织大型接待活动的基础条件。",
                    ),
                    ParagraphContentItem(
                        title = "产业发展",
                        content = "重点发展智能制造、新能源和现代服务业，持续完善园区配套和创新服务体系。",
                    ),
                    ParagraphContentItem(
                        title = "服务保障",
                        content = "围绕会务、交通、住宿、用餐和现场讲解建立了统一调度机制。",
                    ),
                ),
            ),
        )

        return activity
    }

    private fun Person.toJsonSnapshot(): Person =
        Person(
            name = name,
            unit = unit,
            nickName = nickName,
            inspectionTeamItemId = inspectionTeamItemId,
        )

    private companion object {
        private const val MOCK_ACTIVITY_URL = "mock-reception-2026"
        private val logger = LoggerFactory.getLogger(MockDataInitializer::class.java)
    }
}
