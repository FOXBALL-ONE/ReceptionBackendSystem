package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 启动模拟数据初始化器。
 *
 * 应用启动后生成一份覆盖活动、人员、车辆、日程、考察组、用餐、住宿、考察点和市县概况的完整模拟数据。
 */
@Component
class MockDataInitializer(
    private val activitiesRepository: ActivitiesRepository,
    private val colorTagRepository: ColorTagRepository,
    private val personRepository: PersonRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        seedColorTags()

        if (activitiesRepository.existsByUrl(MOCK_ACTIVITY_URL)) {
            return
        }

        val activity = createActivity()
        val savedActivity = activitiesRepository.save(activity)

        personRepository.saveAll(createPeople(savedActivity))
    }

    private fun seedColorTags() {
        listOf(
            ColorTag(name = "重要", color = "#D92D20"),
            ColorTag(name = "接待", color = "#1570EF"),
            ColorTag(name = "考察", color = "#039855"),
            ColorTag(name = "住宿", color = "#7F56D9"),
            ColorTag(name = "用餐", color = "#DC6803"),
            ColorTag(name = "交通", color = "#0086C9"),
        ).filterNot { colorTagRepository.existsByName(it.name.orEmpty()) }
            .forEach(colorTagRepository::save)
    }

    private fun createActivity(): Activities {
        val activity = Activities(
            url = MOCK_ACTIVITY_URL,
            masterTitle = "2026 夏季重点项目接待活动",
            subtitle = "城市更新与产业协同发展专题考察",
            name = "夏季重点项目接待",
            startTime = LocalDateTime.of(2026, 6, 18, 9, 0),
            endTime = LocalDateTime.of(2026, 6, 20, 18, 0),
            bannerTags = "重点项目,城市更新,产业协同,现场考察",
            bannerUrl = "https://example.com/mock/banner/reception-2026.jpg",
            isAnimation = true,
            isTopNavigationBar = true,
            icp = "浙ICP备2026000001号-1",
            technicalSupport = "Foxball 技术支持",
            isOpen = true,
            mealList = createMeals(),
            hostedList = createHostedList(),
            inspectionPoints = createInspectionPoints(),
            overviewOfTheCityAndCounty = createCityOverview(),
        )

        activity.schedules.addAll(createSchedules(activity))
        activity.carList.addAll(createCars(activity))

        return activity
    }

    private fun createMeals(): MutableList<MealItem> = mutableListOf(
        MealItem(
            name = "欢迎午宴",
            description = "本地特色菜品，安排接待组、项目组和嘉宾代表同席交流。",
            position = "市政中心一号宴会厅",
            time = LocalDateTime.of(2026, 6, 18, 12, 0),
        ),
        MealItem(
            name = "工作晚餐",
            description = "围绕产业园区合作事项开展小范围工作沟通。",
            position = "湖畔会议中心二楼餐厅",
            time = LocalDateTime.of(2026, 6, 18, 18, 30),
        ),
        MealItem(
            name = "考察午餐",
            description = "按产业考察组和城市更新考察组分区就餐。",
            position = "数字产业园人才餐厅",
            time = LocalDateTime.of(2026, 6, 19, 12, 10),
        ),
        MealItem(
            name = "返程简餐",
            description = "根据返程车次和航班时间分批安排。",
            position = "接待酒店一楼自助餐厅",
            time = LocalDateTime.of(2026, 6, 20, 11, 30),
        ),
    )

    private fun createHostedList(): MutableList<HostedItem> = mutableListOf(
        HostedItem(
            roomNumber = "A-1208",
            person = personSnapshot("张明", "省发改委", "张主任"),
            hostedColorsTag = HostedColorsTag(name = "贵宾楼", color = "#7F56D9"),
        ),
        HostedItem(
            roomNumber = "A-1210",
            person = personSnapshot("李雪", "市投资促进局", "李处"),
            hostedColorsTag = HostedColorsTag(name = "贵宾楼", color = "#7F56D9"),
        ),
        HostedItem(
            roomNumber = "B-0816",
            person = personSnapshot("王启航", "产业园管委会", "王工"),
            hostedColorsTag = HostedColorsTag(name = "商务楼", color = "#1570EF"),
        ),
        HostedItem(
            roomNumber = "B-0820",
            person = personSnapshot("孙雅", "城市更新公司", "孙经理"),
            hostedColorsTag = HostedColorsTag(name = "商务楼", color = "#1570EF"),
        ),
    )

    private fun createInspectionPoints(): MutableList<InspectionPointItem> = mutableListOf(
        InspectionPointItem(
            imageURL = "https://example.com/mock/inspection/digital-park.jpg",
            description = "数字产业园核心展示区，重点介绍招商成果、企业入驻和智慧园区运营能力。",
        ),
        InspectionPointItem(
            imageURL = "https://example.com/mock/inspection/riverfront.jpg",
            description = "滨水城市更新样板段，展示公共空间改造、慢行系统和文旅消费场景。",
        ),
        InspectionPointItem(
            imageURL = "https://example.com/mock/inspection/manufacturing.jpg",
            description = "先进制造示范工厂，展示自动化产线、质量追溯和绿色制造成果。",
        ),
        InspectionPointItem(
            imageURL = "https://example.com/mock/inspection/service-center.jpg",
            description = "产业服务中心，展示企业服务、项目审批、人才政策和金融支持体系。",
        ),
    )

    private fun createCityOverview(): MutableList<OverviewOfTheCityAndCountyItem> = mutableListOf(
        OverviewOfTheCityAndCountyItem(
            title = "城市概况",
            topImageUrl = "https://example.com/mock/overview/city.jpg",
            description = mutableListOf(
                ParagraphContentItem(
                    title = "区位优势",
                    content = "地处区域交通枢纽，公铁水空联运条件完善，面向长三角主要城市形成两小时通达圈。",
                ),
                ParagraphContentItem(
                    title = "产业基础",
                    content = "形成数字经济、先进制造、现代服务业协同发展的产业格局，重点平台承载能力持续提升。",
                ),
            ),
        ),
        OverviewOfTheCityAndCountyItem(
            title = "县域特色",
            topImageUrl = "https://example.com/mock/overview/county.jpg",
            description = mutableListOf(
                ParagraphContentItem(
                    title = "生态资源",
                    content = "山水资源丰富，生态廊道和滨水空间串联主要功能片区，适合发展文旅和康养产业。",
                ),
                ParagraphContentItem(
                    title = "营商环境",
                    content = "建立重大项目专班服务机制，审批、用地、金融和人才政策形成组合支持。",
                ),
            ),
        ),
    )

    private fun createSchedules(activity: Activities): MutableList<Schedule> = mutableListOf(
        Schedule(
            activity = activity,
            scheduleTags = "抵达,会见,欢迎",
            inspectionTeamItem = mutableListOf(
                InspectionTeamItem(
                    name = "综合接待组",
                    routeUrl = "https://example.com/mock/routes/day-1",
                    scheduleUrl = "https://example.com/mock/schedules/day-1.pdf",
                    routeNode = mutableListOf("高铁东站", "接待酒店", "市政中心", "湖畔会议中心"),
                    eventArrangements = mutableListOf(
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 18, 9, 30),
                            endTime = LocalDateTime.of(2026, 6, 18, 10, 10),
                            content = "高铁站出站迎接并统一乘车",
                            location = "高铁东站 VIP 出站口",
                        ),
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 18, 10, 40),
                            endTime = LocalDateTime.of(2026, 6, 18, 11, 30),
                            content = "领导会见与活动介绍",
                            location = "市政中心第一会议室",
                        ),
                    ),
                ),
            ),
        ),
        Schedule(
            activity = activity,
            scheduleTags = "考察,座谈,签约",
            inspectionTeamItem = mutableListOf(
                InspectionTeamItem(
                    name = "产业考察组",
                    routeUrl = "https://example.com/mock/routes/day-2-industry",
                    scheduleUrl = "https://example.com/mock/schedules/day-2-industry.pdf",
                    routeNode = mutableListOf("接待酒店", "数字产业园", "先进制造示范工厂", "产业服务中心"),
                    eventArrangements = mutableListOf(
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 19, 9, 0),
                            endTime = LocalDateTime.of(2026, 6, 19, 10, 30),
                            content = "考察数字产业园展示中心",
                            location = "数字产业园 A 区",
                        ),
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 19, 14, 30),
                            endTime = LocalDateTime.of(2026, 6, 19, 16, 0),
                            content = "项目合作座谈与备忘录签署",
                            location = "产业服务中心路演厅",
                        ),
                    ),
                ),
                InspectionTeamItem(
                    name = "城市更新考察组",
                    routeUrl = "https://example.com/mock/routes/day-2-city",
                    scheduleUrl = "https://example.com/mock/schedules/day-2-city.pdf",
                    routeNode = mutableListOf("接待酒店", "滨水更新样板段", "历史街区", "文旅综合体"),
                    eventArrangements = mutableListOf(
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 19, 9, 20),
                            endTime = LocalDateTime.of(2026, 6, 19, 11, 0),
                            content = "考察滨水公共空间更新成果",
                            location = "滨水城市客厅",
                        ),
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 19, 15, 0),
                            endTime = LocalDateTime.of(2026, 6, 19, 16, 30),
                            content = "历史街区运营模式交流",
                            location = "文旅综合体三楼会议区",
                        ),
                    ),
                ),
            ),
        ),
        Schedule(
            activity = activity,
            scheduleTags = "复盘,送站,返程",
            inspectionTeamItem = mutableListOf(
                InspectionTeamItem(
                    name = "返程保障组",
                    routeUrl = "https://example.com/mock/routes/day-3",
                    scheduleUrl = "https://example.com/mock/schedules/day-3.pdf",
                    routeNode = mutableListOf("接待酒店", "项目服务中心", "高铁东站", "机场"),
                    eventArrangements = mutableListOf(
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 20, 9, 30),
                            endTime = LocalDateTime.of(2026, 6, 20, 10, 30),
                            content = "活动复盘与后续事项确认",
                            location = "接待酒店二楼会议室",
                        ),
                        EventArrangementsItem(
                            startTime = LocalDateTime.of(2026, 6, 20, 13, 30),
                            endTime = LocalDateTime.of(2026, 6, 20, 15, 30),
                            content = "分批送站送机",
                            location = "酒店大堂集合点",
                        ),
                    ),
                ),
            ),
        ),
    )

    private fun createCars(activity: Activities): MutableList<Car> = mutableListOf(
        Car(
            activity = activity,
            carNumber = 1,
            driver = "赵师傅",
            driverNumber = "13800010001",
            passengersOnBoardList = mutableListOf(
                PassengersOnBoardItem(name = "陈佳", phone = "13900020001"),
                PassengersOnBoardItem(name = "刘海", phone = "13900020002"),
            ),
            passengersList = mutableListOf(
                personSnapshot("张明", "省发改委", "张主任"),
                personSnapshot("李雪", "市投资促进局", "李处"),
            ),
        ),
        Car(
            activity = activity,
            carNumber = 2,
            driver = "钱师傅",
            driverNumber = "13800010002",
            passengersOnBoardList = mutableListOf(
                PassengersOnBoardItem(name = "周宁", phone = "13900020003"),
                PassengersOnBoardItem(name = "吴越", phone = "13900020004"),
            ),
            passengersList = mutableListOf(
                personSnapshot("王启航", "产业园管委会", "王工"),
                personSnapshot("孙雅", "城市更新公司", "孙经理"),
            ),
        ),
        Car(
            activity = activity,
            carNumber = 3,
            driver = "孙师傅",
            driverNumber = "13800010003",
            passengersOnBoardList = mutableListOf(
                PassengersOnBoardItem(name = "郑桐", phone = "13900020005"),
                PassengersOnBoardItem(name = "何林", phone = "13900020006"),
            ),
            passengersList = mutableListOf(
                personSnapshot("陈佳", "接待办", "陈专员"),
                personSnapshot("刘海", "会务公司", "刘主管"),
            ),
        ),
    )

    private fun createPeople(activity: Activities): List<Person> = listOf(
        Person(activity = activity, name = "张明", unit = "省发改委", nickName = "张主任"),
        Person(activity = activity, name = "李雪", unit = "市投资促进局", nickName = "李处"),
        Person(activity = activity, name = "王启航", unit = "产业园管委会", nickName = "王工"),
        Person(activity = activity, name = "孙雅", unit = "城市更新公司", nickName = "孙经理"),
        Person(activity = activity, name = "陈佳", unit = "接待办", nickName = "陈专员"),
        Person(activity = activity, name = "刘海", unit = "会务公司", nickName = "刘主管"),
        Person(activity = activity, name = "周宁", unit = "宣传部", nickName = "周老师"),
        Person(activity = activity, name = "吴越", unit = "后勤保障组", nickName = "吴专员"),
        Person(activity = activity, name = "郑桐", unit = "项目服务中心", nickName = "郑经理"),
        Person(activity = activity, name = "何林", unit = "安保联络组", nickName = "何队"),
    )

    private fun personSnapshot(
        name: String,
        unit: String,
        nickName: String,
    ): Person = Person(
        name = name,
        unit = unit,
        nickName = nickName,
    )

    private companion object {
        const val MOCK_ACTIVITY_URL = "/activities/2026-summer-reception"
    }
}
