package top.foxball.receptionbackendsystem.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
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
import top.foxball.receptionbackendsystem.datasource.jdbc.InspectionTeamItinerary
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

/**
 * 接待活动模拟数据初始化器。
 *
 * 在应用就绪后向数据库写入一份完整的演示数据，覆盖活动及其全部子实体，
 * 用于本地开发、接口联调和前端页面预览。已存在同 url 活动时跳过初始化。
 *
 * 实体关系与模拟规模概览：
 * - Activities        ×1  聚合根，子实体全部通过 activity_id 关联并级联删除。
 * - ColorTag          ×3 PERSON + ×3 LODGING，被 Person / Lodging 复用，PromptService 以色值引用。
 * - Person            ×6  分属 3 个人员颜色标签，绑定考察组 id（松耦合，仅存 id）。
 * - InspectionTeamItem×3  各含 3 天路线节点与事件安排；只归属 activity，避免与 Schedule 重复持久化。
 * - Schedule          ×3  对应活动三天日程，考察组通过 activity 侧统一管理。
 * - Car               ×3  各含随车人员值对象与乘客人员快照（jsonb）。
 * - Meal              ×3  对应三天用餐安排。
 * - Lodging           ×3  绑定住宿颜色标签与入住人员快照。
 * - InspectionPoint   ×3  考察点位说明。
 * - Image             ×3  banner / inspection / overview 三类图片元数据。
 * - Overview          ×1  市县概况，含 3 段图文。
 * - PromptService     ×1  含 3 组工作人员、3 条注意事项、3 条天气提示及出席说明。
 */
@Component
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
            "Mock data initialized: activityId={}, url={}, colorTags={}, persons={}, inspectionTeams={}, schedules={}, cars={}, meals={}, lodgings={}, inspectionPoints={}, images={}, overviews={}, promptServices={}",
            savedActivity.id,
            savedActivity.url,
            savedActivity.colorTagList.size,
            savedActivity.personList.size,
            savedActivity.inspectionTeamItemList.size,
            savedActivity.schedules.size,
            savedActivity.carList.size,
            savedActivity.mealList.size,
            savedActivity.hostedList.size,
            savedActivity.inspectionPoints.size,
            savedActivity.imageList.size,
            savedActivity.overviewOfTheCityAndCounty.size,
            savedActivity.promptServiceList.size,
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
            bannerUrl = "/images/mock/reception/banner.jpg",
            isAnimation = true,
            isTopNavigationBar = true,
            icp = "京ICP备20260001号",
            technicalSupport = "技术支持：数字接待系统",
            isOpen = true,
        )

        // ---------- 颜色标签：3 PERSON + 3 LODGING ----------
        val personBlueTag = ColorTag(activity = activity, name = "综合协调组", color = "#2563EB", type = ColorTag.TYPE_PERSON)
        val personGreenTag = ColorTag(activity = activity, name = "现场保障组", color = "#16A34A", type = ColorTag.TYPE_PERSON)
        val personPurpleTag = ColorTag(activity = activity, name = "会务讲解组", color = "#7C3AED", type = ColorTag.TYPE_PERSON)
        val lodgingBlueTag = ColorTag(activity = activity, name = "综合协调组", color = "#2563EB", type = ColorTag.TYPE_LODGING)
        val lodgingGreenTag = ColorTag(activity = activity, name = "现场保障组", color = "#16A34A", type = ColorTag.TYPE_LODGING)
        val lodgingAmberTag = ColorTag(activity = activity, name = "会务服务组", color = "#D97706", type = ColorTag.TYPE_LODGING)
        activity.colorTagList.addAll(
            listOf(personBlueTag, personGreenTag, personPurpleTag, lodgingBlueTag, lodgingGreenTag, lodgingAmberTag),
        )

        // ---------- 日程：3 天（天注册表，考察组跨天共用身份，各天行程独立） ----------
        val day1 = Schedule(activity = activity, scheduleTags = "第一天,项目观摩,上午")
        val day2 = Schedule(activity = activity, scheduleTags = "第二天,城市更新,全天")
        val day3 = Schedule(activity = activity, scheduleTags = "第三天,民生服务,上午")
        activity.schedules.addAll(listOf(day1, day2, day3))

        // ---------- 考察组：3 个身份，每个身份包含 3 天行程，每天 3 个安排 ----------
        fun itinerary(
            schedule: Schedule,
            dayOffset: Long,
            routeUrl: String,
            scheduleUrl: String,
            routeNode: List<String>,
            eventSpecs: List<EventSpec>,
        ) = InspectionTeamItinerary(
            inspectionTeam = null,
            schedule = schedule,
            routeUrl = routeUrl,
            scheduleUrl = scheduleUrl,
            routeNode = routeNode.toMutableList(),
            eventArrangements = eventSpecs.map { spec ->
                EventArrangementsItem(
                    startTime = startTime.plusDays(dayOffset).withHour(spec.startHour).withMinute(spec.startMinute),
                    endTime = startTime.plusDays(dayOffset).withHour(spec.endHour).withMinute(spec.endMinute),
                    content = spec.content,
                    location = spec.location,
                )
            }.toMutableList(),
        )

        val projectTeam = InspectionTeamItem(
            activity = activity,
            name = "项目观摩组",
            itineraries = mutableListOf(
                itinerary(
                    schedule = day1,
                    dayOffset = 0,
                    routeUrl = "/mock/routes/project-team-day1.pdf",
                    scheduleUrl = "/mock/schedules/project-team-day1.pdf",
                    routeNode = listOf("政务中心", "智能制造园", "新能源基地", "项目展厅"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 30,
                            endHour = 10,
                            endMinute = 30,
                            content = "听取重点项目建设情况汇报",
                            location = "政务中心第一会议室",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 45,
                            endHour = 12,
                            endMinute = 0,
                            content = "观摩智能制造生产线",
                            location = "智能制造园一号车间",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 30,
                            content = "调研新能源基地储能项目",
                            location = "新能源基地展厅",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day2,
                    dayOffset = 1,
                    routeUrl = "/mock/routes/project-team-day2.pdf",
                    scheduleUrl = "/mock/schedules/project-team-day2.pdf",
                    routeNode = listOf("项目指挥部", "产业配套园", "创新孵化中心", "企业服务大厅"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 15,
                            content = "听取重大项目推进机制介绍",
                            location = "项目指挥部调度室",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 30,
                            endHour = 11,
                            endMinute = 45,
                            content = "考察产业链配套服务平台",
                            location = "产业配套园服务中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 30,
                            endHour = 16,
                            endMinute = 0,
                            content = "座谈项目招引与企业服务工作",
                            location = "企业服务大厅三层会议室",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day3,
                    dayOffset = 2,
                    routeUrl = "/mock/routes/project-team-day3.pdf",
                    scheduleUrl = "/mock/schedules/project-team-day3.pdf",
                    routeNode = listOf("数字经济产业园", "科创路演中心", "综合交通枢纽", "总结会场"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 10,
                            content = "调研数字经济产业园运营情况",
                            location = "数字经济产业园展示中心",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 25,
                            endHour = 11,
                            endMinute = 40,
                            content = "观摩科创企业路演成果",
                            location = "科创路演中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 30,
                            content = "召开项目观摩组总结交流会",
                            location = "政务中心第二会议室",
                        ),
                    ),
                ),
            ),
        ).also { team -> team.itineraries.forEach { it.inspectionTeam = team } }

        val cityTeam = InspectionTeamItem(
            activity = activity,
            name = "城市更新组",
            itineraries = mutableListOf(
                itinerary(
                    schedule = day1,
                    dayOffset = 0,
                    routeUrl = "/mock/routes/city-team-day1.pdf",
                    scheduleUrl = "/mock/schedules/city-team-day1.pdf",
                    routeNode = listOf("城市展馆", "更新示范街区", "口袋公园", "历史街巷"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 15,
                            endHour = 10,
                            endMinute = 15,
                            content = "参观城市规划与更新成果展",
                            location = "城市展馆",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 30,
                            endHour = 11,
                            endMinute = 45,
                            content = "调研更新示范街区改造情况",
                            location = "更新示范街区",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 20,
                            content = "考察口袋公园便民设施建设",
                            location = "青年路口袋公园",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day2,
                    dayOffset = 1,
                    routeUrl = "/mock/routes/city-team-day2.pdf",
                    scheduleUrl = "/mock/schedules/city-team-day2.pdf",
                    routeNode = listOf("城市展馆", "更新示范街区", "公共服务中心"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 30,
                            content = "参观城市更新成果展",
                            location = "城市展馆",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 45,
                            endHour = 11,
                            endMinute = 45,
                            content = "调研便民服务建设",
                            location = "公共服务中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 30,
                            endHour = 16,
                            endMinute = 0,
                            content = "走访更新示范街区便民商户",
                            location = "更新示范街区",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day3,
                    dayOffset = 2,
                    routeUrl = "/mock/routes/city-team-day3.pdf",
                    scheduleUrl = "/mock/schedules/city-team-day3.pdf",
                    routeNode = listOf("海绵城市示范区", "智慧停车平台", "城市管理指挥中心", "总结会场"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 0,
                            content = "调研海绵城市示范区建设",
                            location = "滨河海绵城市示范区",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 15,
                            endHour = 11,
                            endMinute = 30,
                            content = "了解智慧停车平台运行情况",
                            location = "智慧停车运营中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 30,
                            content = "召开城市更新组总结交流会",
                            location = "城市管理指挥中心会议室",
                        ),
                    ),
                ),
            ),
        ).also { team -> team.itineraries.forEach { it.inspectionTeam = team } }

        val serviceTeam = InspectionTeamItem(
            activity = activity,
            name = "民生服务组",
            itineraries = mutableListOf(
                itinerary(
                    schedule = day1,
                    dayOffset = 0,
                    routeUrl = "/mock/routes/service-team-day1.pdf",
                    scheduleUrl = "/mock/schedules/service-team-day1.pdf",
                    routeNode = listOf("政务服务大厅", "市民热线中心", "社区卫生服务站", "养老服务中心"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 0,
                            content = "体验政务服务大厅一窗办理流程",
                            location = "政务服务大厅",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 15,
                            endHour = 11,
                            endMinute = 15,
                            content = "了解市民热线受理转办机制",
                            location = "市民热线中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 30,
                            content = "调研社区卫生便民服务",
                            location = "东湖社区卫生服务站",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day2,
                    dayOffset = 1,
                    routeUrl = "/mock/routes/service-team-day2.pdf",
                    scheduleUrl = "/mock/schedules/service-team-day2.pdf",
                    routeNode = listOf("社区党群服务中心", "便民服务驿站", "智慧养老平台", "公共文化空间"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 10,
                            content = "观摩社区党群服务开展情况",
                            location = "社区党群服务中心",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 25,
                            endHour = 11,
                            endMinute = 30,
                            content = "调研便民服务驿站运行情况",
                            location = "邻里便民服务驿站",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 30,
                            endHour = 16,
                            endMinute = 0,
                            content = "了解智慧养老平台服务场景",
                            location = "智慧养老服务中心",
                        ),
                    ),
                ),
                itinerary(
                    schedule = day3,
                    dayOffset = 2,
                    routeUrl = "/mock/routes/service-team-day3.pdf",
                    scheduleUrl = "/mock/schedules/service-team-day3.pdf",
                    routeNode = listOf("市民热线中心", "社区党群服务中心", "智慧城市运行大厅"),
                    eventSpecs = listOf(
                        EventSpec(
                            startHour = 9,
                            startMinute = 0,
                            endHour = 10,
                            endMinute = 0,
                            content = "了解市民热线接办流程",
                            location = "市民热线中心",
                        ),
                        EventSpec(
                            startHour = 10,
                            startMinute = 15,
                            endHour = 11,
                            endMinute = 30,
                            content = "观摩社区党群服务开展情况",
                            location = "社区党群服务中心",
                        ),
                        EventSpec(
                            startHour = 14,
                            startMinute = 0,
                            endHour = 15,
                            endMinute = 30,
                            content = "召开民生服务组总结交流会",
                            location = "智慧城市运行大厅会议室",
                        ),
                    ),
                ),
            ),
        ).also { team -> team.itineraries.forEach { it.inspectionTeam = team } }

        // 考察组身份只归属 activity，各天行程挂到具体天，避免与 Schedule 集合重复持久化。
        activity.inspectionTeamItemList.addAll(listOf(projectTeam, cityTeam, serviceTeam))

        // ---------- 人员：6 个，分属 3 个 PERSON 标签，绑定考察组 id ----------
        val personA = Person(activity = activity, name = "张明", unit = "省发展改革委", nickName = "张主任", colorTag = personBlueTag)
        val personB = Person(activity = activity, name = "李娜", unit = "省工业和信息化厅", nickName = "李处长", colorTag = personBlueTag)
        val personC = Person(activity = activity, name = "王强", unit = "市政府办公室", nickName = "王主任", colorTag = personGreenTag)
        val personD = Person(activity = activity, name = "赵琳", unit = "市招商服务中心", nickName = "赵经理", colorTag = personGreenTag)
        val personE = Person(activity = activity, name = "孙伟", unit = "市接待服务中心", nickName = "孙科长", colorTag = personPurpleTag)
        val personF = Person(activity = activity, name = "周敏", unit = "市文化广电旅游局", nickName = "周讲解", colorTag = personPurpleTag)
        activity.personList.addAll(listOf(personA, personB, personC, personD, personE, personF))

        // 人员与考察组的关联通过 inspection_team_item_id 松耦合体现，由业务层在保存后按需回填，
        // 此处仅准备人员快照，供车辆乘客名单与住宿记录引用。
        val snapshotA = personA.toJsonSnapshot()
        val snapshotB = personB.toJsonSnapshot()
        val snapshotC = personC.toJsonSnapshot()
        val snapshotD = personD.toJsonSnapshot()
        val snapshotE = personE.toJsonSnapshot()
        val snapshotF = personF.toJsonSnapshot()

        // ---------- 车辆：3 辆，各含随车人员和乘客快照 ----------
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
                    passengersList = mutableListOf(snapshotA, snapshotB),
                ),
                Car(
                    activity = activity,
                    carNumber = 2,
                    carPlate = "京B·R2026",
                    driver = "孙师傅",
                    driverNumber = "13800000002",
                    passengersOnBoardList = mutableListOf(
                        PassengersOnBoardItem(name = "随车联络员-吴昊", phone = "13900000003"),
                        PassengersOnBoardItem(name = "讲解员-韩雪", phone = "13900000004"),
                    ),
                    passengersList = mutableListOf(snapshotC, snapshotD),
                ),
                Car(
                    activity = activity,
                    carNumber = 3,
                    carPlate = "京C·T2026",
                    driver = "马师傅",
                    driverNumber = "13800000003",
                    passengersOnBoardList = mutableListOf(
                        PassengersOnBoardItem(name = "随车联络员-林峰", phone = "13900000005"),
                    ),
                    passengersList = mutableListOf(snapshotE, snapshotF),
                ),
            ),
        )

        // ---------- 用餐：3 餐 ----------
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

        // ---------- 住宿：3 间，绑定 LODGING 标签与人员快照 ----------
        activity.hostedList.addAll(
            listOf(
                Lodging(activity = activity, roomNumber = "1801", person = snapshotA, colorTag = lodgingBlueTag),
                Lodging(activity = activity, roomNumber = "1802", person = snapshotB, colorTag = lodgingBlueTag),
                Lodging(activity = activity, roomNumber = "1811", person = snapshotC, colorTag = lodgingGreenTag),
            ),
        )

        // ---------- 考察点：3 个 ----------
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

        // ---------- 图片：3 张（banner / inspection / overview） ----------
        activity.imageList.addAll(
            listOf(
                mockImage(
                    activity = activity,
                    originalName = "mock-banner.jpg",
                    storedName = "mock-banner-2026.jpg",
                    width = 1920,
                    height = 1080,
                    size = 1_245_600L,
                    objectKey = "mock/reception/banner.jpg",
                    usageType = "banner",
                    now = now,
                ),
                mockImage(
                    activity = activity,
                    originalName = "mock-project.jpg",
                    storedName = "mock-project-2026.jpg",
                    width = 1600,
                    height = 900,
                    size = 982_300L,
                    objectKey = "mock/reception/project.jpg",
                    usageType = "inspection",
                    now = now,
                ),
                mockImage(
                    activity = activity,
                    originalName = "mock-overview.jpg",
                    storedName = "mock-overview-2026.jpg",
                    width = 1600,
                    height = 900,
                    size = 761_400L,
                    objectKey = "mock/reception/overview.jpg",
                    usageType = "overview",
                    now = now,
                ),
            ),
        )

        // ---------- 市县概况：1 条，含 3 段 ----------
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

        // ---------- 提示服务：1 条，含 3 工作人员组 + 3 注意事项 + 3 天气 ----------
        activity.promptServiceList.add(
            PromptService(
                activity = activity,
                staffList = mutableListOf(
                    StaffItem(
                        name = "综合协调组",
                        colorTag = personBlueTag.color,
                        groupList = mutableListOf(
                            OneStaff(name = "刘洋", duty = "总协调", phone = "13900000001"),
                            OneStaff(name = "周敏", duty = "资料统筹", phone = "13900000002"),
                        ),
                    ),
                    StaffItem(
                        name = "现场保障组",
                        colorTag = personGreenTag.color,
                        groupList = mutableListOf(
                            OneStaff(name = "吴昊", duty = "现场保障", phone = "13900000003"),
                            OneStaff(name = "韩雪", duty = "车辆调度", phone = "13900000004"),
                        ),
                    ),
                    StaffItem(
                        name = "会务讲解组",
                        colorTag = personPurpleTag.color,
                        groupList = mutableListOf(
                            OneStaff(name = "林峰", duty = "会务主持", phone = "13900000005"),
                            OneStaff(name = "白璐", duty = "现场讲解", phone = "13900000006"),
                        ),
                    ),
                ),
                noteList = mutableListOf(
                    NoteItem(title = "集合提醒", content = "请各组提前15分钟到达指定集合点。", colorTag = lodgingAmberTag.color),
                    NoteItem(title = "资料准备", content = "讲解材料、车辆牌号和住宿信息已同步至接待页面。", colorTag = personBlueTag.color),
                    NoteItem(title = "安全须知", content = "现场观摩请服从引导，注意生产区域安全标识。", colorTag = personGreenTag.color),
                ),
                weatherList = mutableListOf(
                    WeatherItem(time = startTime, city = "北京市", temperature = "20°C - 28°C", weatherDescriptor = "晴"),
                    WeatherItem(time = startTime.plusDays(1), city = "北京市", temperature = "19°C - 27°C", weatherDescriptor = "多云"),
                    WeatherItem(time = startTime.plusDays(2), city = "北京市", temperature = "21°C - 29°C", weatherDescriptor = "晴转多云"),
                ),
                attendanceInstructionsMode = true,
                attendanceInstructionsTitle = "出席说明",
                attendanceInstructionsContent = "请参会人员按日程安排参加活动，如需调整车辆或住宿，请联系综合协调组。",
            ),
        )

        return activity
    }

    private data class EventSpec(
        val startHour: Int,
        val startMinute: Int,
        val endHour: Int,
        val endMinute: Int,
        val content: String,
        val location: String,
    )

    private fun mockImage(
        activity: Activities,
        originalName: String,
        storedName: String,
        width: Int,
        height: Int,
        size: Long,
        objectKey: String,
        usageType: String,
        now: LocalDateTime,
    ): Image {
        val shaSeed = objectKey.hashCode().toString()
        return Image(
            activity = activity,
            originalFilename = originalName,
            storedFilename = storedName,
            extension = "jpg",
            contentType = "image/jpeg",
            fileSize = size,
            width = width,
            height = height,
            sha256 = shaSeed.padEnd(64, '0').take(64),
            bucket = "mock-images",
            objectKey = objectKey,
            storagePath = "./images/$objectKey",
            accessUrl = "/images/$objectKey",
            uploadedBy = "mock",
            usageType = usageType,
            isDeleted = false,
            createdAt = now,
            updatedAt = now,
        )
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
