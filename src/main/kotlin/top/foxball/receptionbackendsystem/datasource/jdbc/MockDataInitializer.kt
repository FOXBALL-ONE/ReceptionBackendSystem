package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.config.ImageProperties
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import javax.imageio.ImageIO

/**
 * 启动模拟数据初始化器。
 *
 * 应用启动后生成一份覆盖活动、人员、车辆、日程、考察组、用餐、住宿、考察点、市县概况和提示服务的完整模拟数据。
 */
@Component
class MockDataInitializer(
    private val activitiesRepository: ActivitiesRepository,
    private val colorTagRepository: ColorTagRepository,
    private val personRepository: PersonRepository,
    private val promptServiceRepository: PromptServiceRepository,
    private val imageRepository: ImageRepository,
    private val imageProperties: ImageProperties,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        seedColorTags()
        seedPromptService()
        val mockImages = seedMockImages()

        if (activitiesRepository.existsByUrl(MOCK_ACTIVITY_URL)) {
            return
        }

        val savedActivity = activitiesRepository.save(createActivity(mockImages))
        personRepository.saveAll(createPeople(savedActivity))
    }

    /** 初始化颜色标签，按标签名幂等插入。 */
    private fun seedColorTags() {
        COLOR_TAGS
            .filterNot { colorTagRepository.existsByName(it.name) }
            .map { ColorTag(name = it.name, color = it.color) }
            .forEach(colorTagRepository::save)
    }

    /** 初始化提示服务配置，已有配置时不重复生成。 */
    private fun seedPromptService() {
        if (promptServiceRepository.count() > 0) {
            return
        }

        promptServiceRepository.save(
            PromptService(
                staffList = mutableListOf(
                    StaffItem(
                        name = "现场统筹",
                        colorTag = "接待",
                        groupList = mutableListOf(
                            OneStaff(name = "陈佳", duty = "总协调", phone = "13900020001"),
                            OneStaff(name = "刘海", duty = "会务联络", phone = "13900020002"),
                        ),
                    ),
                    StaffItem(
                        name = "交通保障",
                        colorTag = "交通",
                        groupList = mutableListOf(
                            OneStaff(name = "周宁", duty = "车辆调度", phone = "13900020003"),
                            OneStaff(name = "吴越", duty = "站点接驳", phone = "13900020004"),
                        ),
                    ),
                    StaffItem(
                        name = "住宿服务",
                        colorTag = "住宿",
                        groupList = mutableListOf(
                            OneStaff(name = "郑桐", duty = "酒店协调", phone = "13900020005"),
                            OneStaff(name = "何林", duty = "夜间值守", phone = "13900020006"),
                        ),
                    ),
                    StaffItem(
                        name = "医疗保障",
                        colorTag = "重要",
                        groupList = mutableListOf(
                            OneStaff(name = "宋洁", duty = "随队医生", phone = "13900020007"),
                            OneStaff(name = "马远", duty = "应急联络", phone = "13900020008"),
                        ),
                    ),
                ),
                noteList = mutableListOf(
                    NoteItem(
                        title = "证件携带",
                        content = "进入园区和会场请随身携带有效身份证件。",
                        colorTag = "重要",
                    ),
                    NoteItem(
                        title = "集合提醒",
                        content = "各组请提前 10 分钟到达指定上车点，车辆按车号有序发车。",
                        colorTag = "交通",
                    ),
                    NoteItem(
                        title = "着装建议",
                        content = "现场考察包含步行参观点位，建议穿着舒适鞋履，并按天气情况携带雨具。",
                        colorTag = "接待",
                    ),
                    NoteItem(
                        title = "用餐提示",
                        content = "如有清真、素食或其他特殊用餐需求，请提前联系会务联络人员登记。",
                        colorTag = "用餐",
                    ),
                    NoteItem(
                        title = "住宿服务",
                        content = "酒店前台已预留活动房卡，入住时请报姓名领取；退房时间以现场通知为准。",
                        colorTag = "住宿",
                    ),
                    NoteItem(
                        title = "应急联系",
                        content = "如遇身体不适、行李遗失或行程调整，请第一时间联系现场统筹或医疗保障人员。",
                        colorTag = "重要",
                    ),
                ),
                weatherList = mutableListOf(
                    WeatherItem(
                        time = at(0, 8, 0),
                        city = "示范市",
                        temperature = "26-32 C",
                        weatherDescriptor = "多云，午后局地阵雨",
                    ),
                    WeatherItem(
                        time = at(1, 8, 0),
                        city = "示范市",
                        temperature = "25-31 C",
                        weatherDescriptor = "阴转多云",
                    ),
                    WeatherItem(
                        time = at(2, 8, 0),
                        city = "示范市",
                        temperature = "24-30 C",
                        weatherDescriptor = "小雨转多云，体感较舒适",
                    ),
                ),
                attendanceInstructionsMode = false,
                attendanceInstructionsTitle = "温馨提示",
                attendanceInstructionsContent = "请各参会人员按日程安排准时参加，临时调整以现场接待组通知为准。",
            )
        )
    }

    /** 创建活动主数据，并挂载可级联保存的子数据。 */
    private fun createActivity(mockImages: Map<String, String>): Activities {
        val activity = Activities(
            url = MOCK_ACTIVITY_URL,
            masterTitle = "2026 夏季重点项目接待活动",
            subtitle = "城市更新与产业协同发展专题考察",
            name = "夏季重点项目接待",
            startTime = at(0, 9, 0),
            endTime = at(2, 18, 0),
            bannerTags = "重点项目,城市更新,产业协同,现场考察",
            bannerUrl = mockImagePath(mockImages, "0.png"),
            isAnimation = true,
            isTopNavigationBar = true,
            icp = "浙ICP备2026000001号-1",
            technicalSupport = "Foxball 技术支持",
            isOpen = true,
            mealList = createMeals(),
            hostedList = createHostedList(),
            inspectionPoints = createInspectionPoints(mockImages),
            overviewOfTheCityAndCounty = createCityOverview(mockImages),
        )

        activity.schedules.addAll(createSchedules(activity, mockImages))
        activity.carList.addAll(createCars(activity))

        return activity
    }

    private fun createMeals(): MutableList<MealItem> = mutableListOf(
        meal("欢迎午宴", "本地特色菜品，安排接待组、项目组和嘉宾代表同席交流。", "市政中心一号宴会厅", 0, 12, 0),
        meal("工作晚餐", "围绕产业园区合作事项开展小范围工作沟通。", "湖畔会议中心二楼餐厅", 0, 18, 30),
        meal("考察午餐", "按产业考察组和城市更新考察组分区就餐。", "数字产业园人才餐厅", 1, 12, 10),
        meal("返程简餐", "根据返程车次和航班时间分批安排。", "接待酒店一楼自助餐厅", 2, 11, 30),
    )

    private fun createHostedList(): MutableList<HostedItem> = mutableListOf(
        hosted("A-1208", "张明", "贵宾楼", "#7F56D9"),
        hosted("A-1210", "李雪", "贵宾楼", "#7F56D9"),
        hosted("B-0816", "王启航", "商务楼", "#1570EF"),
        hosted("B-0820", "孙雅", "商务楼", "#1570EF"),
    )

    private fun createInspectionPoints(mockImages: Map<String, String>): MutableList<InspectionPointItem> = mutableListOf(
        inspectionPoint(
            mockImages,
            "1.jpg",
            "数字产业园核心展示区，重点介绍招商成果、企业入驻和智慧园区运营能力。",
        ),
        inspectionPoint(
            mockImages,
            "7.png",
            "滨水城市更新样板段，展示公共空间改造、慢行系统和文旅消费场景。",
        ),
        inspectionPoint(
            mockImages,
            "9.png",
            "先进制造示范工厂，展示自动化产线、质量追溯和绿色制造成果。",
        ),
        inspectionPoint(
            mockImages,
            "0.png",
            "产业服务中心，展示企业服务、项目审批、人才政策和金融支持体系。",
        ),
    )

    private fun createCityOverview(mockImages: Map<String, String>): MutableList<OverviewOfTheCityAndCountyItem> = mutableListOf(
        OverviewOfTheCityAndCountyItem(
            title = "城市概况",
            topImageUrl = mockImagePath(mockImages, "7.png"),
            description = mutableListOf(
                paragraph("区位优势", "地处区域交通枢纽，公铁水空联运条件完善，面向长三角主要城市形成两小时通达圈。"),
                paragraph("产业基础", "形成数字经济、先进制造、现代服务业协同发展的产业格局，重点平台承载能力持续提升。"),
            ),
        ),
        OverviewOfTheCityAndCountyItem(
            title = "县域特色",
            topImageUrl = mockImagePath(mockImages, "9.png"),
            description = mutableListOf(
                paragraph("生态资源", "山水资源丰富，生态廊道和滨水空间串联主要功能片区，适合发展文旅和康养产业。"),
                paragraph("营商环境", "建立重大项目专班服务机制，审批、用地、金融和人才政策形成组合支持。"),
            ),
        ),
    )

    private fun createSchedules(activity: Activities, mockImages: Map<String, String>): MutableList<Schedule> = mutableListOf(
        schedule(
            activity = activity,
            tags = "抵达,会见,欢迎",
            teams = listOf(
                inspectionTeam(
                    name = "综合接待组",
                    mockImages = mockImages,
                    routeImageName = "1.jpg",
                    routeNode = listOf("高铁东站", "接待酒店", "市政中心", "湖畔会议中心"),
                    events = listOf(
                        event(0, 9, 30, 10, 10, "高铁站出站迎接并统一乘车", "高铁东站 VIP 出站口"),
                        event(0, 10, 40, 11, 30, "领导会见与活动介绍", "市政中心第一会议室"),
                    ),
                ),
            ),
        ),
        schedule(
            activity = activity,
            tags = "考察,座谈,签约",
            teams = listOf(
                inspectionTeam(
                    name = "产业考察组",
                    mockImages = mockImages,
                    routeImageName = "7.png",
                    routeNode = listOf("接待酒店", "数字产业园", "先进制造示范工厂", "产业服务中心"),
                    events = listOf(
                        event(1, 9, 0, 10, 30, "考察数字产业园展示中心", "数字产业园 A 区"),
                        event(1, 14, 30, 16, 0, "项目合作座谈与备忘录签署", "产业服务中心路演厅"),
                    ),
                ),
                inspectionTeam(
                    name = "城市更新考察组",
                    mockImages = mockImages,
                    routeImageName = "9.png",
                    routeNode = listOf("接待酒店", "滨水更新样板段", "历史街区", "文旅综合体"),
                    events = listOf(
                        event(1, 9, 20, 11, 0, "考察滨水公共空间更新成果", "滨水城市客厅"),
                        event(1, 15, 0, 16, 30, "历史街区运营模式交流", "文旅综合体三楼会议区"),
                    ),
                ),
            ),
        ),
        schedule(
            activity = activity,
            tags = "复盘,送站,返程",
            teams = listOf(
                inspectionTeam(
                    name = "返程保障组",
                    mockImages = mockImages,
                    routeImageName = "0.png",
                    routeNode = listOf("接待酒店", "项目服务中心", "高铁东站", "机场"),
                    events = listOf(
                        event(2, 9, 30, 10, 30, "活动复盘与后续事项确认", "接待酒店二楼会议室"),
                        event(2, 13, 30, 15, 30, "分批送站送机", "酒店大堂集合点"),
                    ),
                ),
            ),
        ),
    )

    private fun createCars(activity: Activities): MutableList<Car> = mutableListOf(
        car(
            activity = activity,
            carNumber = 1,
            driver = "赵师傅",
            driverNumber = "13800010001",
            onBoardNames = listOf("陈佳", "刘海"),
            passengerNames = listOf("张明", "李雪"),
        ),
        car(
            activity = activity,
            carNumber = 2,
            driver = "钱师傅",
            driverNumber = "13800010002",
            onBoardNames = listOf("周宁", "吴越"),
            passengerNames = listOf("王启航", "孙雅"),
        ),
        car(
            activity = activity,
            carNumber = 3,
            driver = "孙师傅",
            driverNumber = "13800010003",
            onBoardNames = listOf("郑桐", "何林"),
            passengerNames = listOf("陈佳", "刘海"),
        ),
    )

    private fun createPeople(activity: Activities): List<Person> = PERSON_SEEDS.map { seed ->
        seed.toPerson(activity)
    }

    private fun meal(
        name: String,
        description: String,
        position: String,
        dayOffset: Long,
        hour: Int,
        minute: Int,
    ) = MealItem(
        name = name,
        description = description,
        position = position,
        time = at(dayOffset, hour, minute),
    )

    private fun hosted(
        roomNumber: String,
        personName: String,
        tagName: String,
        tagColor: String,
    ) = HostedItem(
        roomNumber = roomNumber,
        person = personSnapshot(personName),
        hostedColorsTag = HostedColorsTag(name = tagName, color = tagColor),
    )

    private fun inspectionPoint(
        mockImages: Map<String, String>,
        imageName: String,
        description: String,
    ) = InspectionPointItem(
        imageURL = mockImagePath(mockImages, imageName),
        description = description,
    )

    private fun paragraph(
        title: String,
        content: String,
    ) = ParagraphContentItem(
        title = title,
        content = content,
    )

    private fun schedule(
        activity: Activities,
        tags: String,
        teams: List<InspectionTeamItem>,
    ) = Schedule(
        activity = activity,
        scheduleTags = tags,
        inspectionTeamItem = teams.toMutableList(),
    )

    private fun inspectionTeam(
        name: String,
        mockImages: Map<String, String>,
        routeImageName: String,
        routeNode: List<String>,
        events: List<EventArrangementsItem>,
    ) = InspectionTeamItem(
        name = name,
        routeUrl = mockImagePath(mockImages, routeImageName),
        routeNode = routeNode.toMutableList(),
        eventArrangements = events.toMutableList(),
    )

    private fun event(
        dayOffset: Long,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        content: String,
        location: String,
    ) = EventArrangementsItem(
        startTime = at(dayOffset, startHour, startMinute),
        endTime = at(dayOffset, endHour, endMinute),
        content = content,
        location = location,
    )

    private fun car(
        activity: Activities,
        carNumber: Long,
        driver: String,
        driverNumber: String,
        onBoardNames: List<String>,
        passengerNames: List<String>,
    ) = Car(
        activity = activity,
        carNumber = carNumber,
        driver = driver,
        driverNumber = driverNumber,
        passengersOnBoardList = onBoardNames.map(::passengerOnBoard).toMutableList(),
        passengersList = passengerNames.map(::personSnapshot).toMutableList(),
    )

    private fun passengerOnBoard(name: String): PassengersOnBoardItem = PassengersOnBoardItem(
        name = name,
        phone = personPhone(name),
    )

    private fun personSnapshot(name: String): Person = personSeed(name).toPerson()

    private fun personPhone(name: String): String = personSeed(name).phone

    private fun personSeed(name: String): PersonSeed = PERSON_SEEDS_BY_NAME[name]
        ?: error("模拟人员不存在：$name")

    /** 将项目 image 目录中的图片导入到配置的图片存储目录，并写入图片元数据表。 */
    private fun seedMockImages(): Map<String, String> {
        val sourceDir = mockImageSourceDir() ?: return emptyMap()
        val importedImages = linkedMapOf<String, String>()

        Files.newDirectoryStream(sourceDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.isImageFile() }
                .sortedBy { it.fileName.toString() }
                .forEach { sourcePath ->
                    val fileName = sourcePath.fileName.toString()
                    importedImages[fileName] = importMockImage(sourcePath)
                }
        }

        return importedImages
    }

    /** 将单张模拟图片复制到 Image.storage/mock 下，并创建或更新对应元数据。 */
    private fun importMockImage(sourcePath: Path): String {
        val fileName = sourcePath.fileName.toString()
        val relativePath = imageProperties.normalizeRelativePath("mock/$fileName")
        val targetPath = imageProperties.resolveStoragePath(relativePath)

        Files.createDirectories(targetPath.parent)
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

        val imageSize = runCatching { ImageIO.read(targetPath.toFile()) }.getOrNull()
        val image = imageRepository.findByObjectKeyAndIsDeletedFalse(relativePath) ?: Image(
            originalFilename = fileName,
            storedFilename = fileName,
            objectKey = relativePath,
            storagePath = relativePath,
            accessUrl = relativePath,
            usageType = MOCK_IMAGE_USAGE_TYPE,
        )

        image.originalFilename = fileName
        image.storedFilename = fileName
        image.extension = sourcePath.extension()
        image.contentType = Files.probeContentType(targetPath) ?: image.contentTypeFromExtension()
        image.fileSize = Files.size(targetPath)
        image.width = imageSize?.width
        image.height = imageSize?.height
        image.sha256 = sha256(targetPath)
        image.objectKey = relativePath
        image.storagePath = relativePath
        image.accessUrl = relativePath
        image.usageType = MOCK_IMAGE_USAGE_TYPE
        image.isDeleted = false

        imageRepository.save(image)
        return relativePath
    }

    private fun mockImagePath(mockImages: Map<String, String>, imageName: String): String? = mockImages[imageName]

    /** 按常见位置查找项目内的 image 目录，兼容源码目录和项目根目录两种放置方式。 */
    private fun mockImageSourceDir(): Path? = MOCK_IMAGE_SOURCE_DIRS
        .map { Paths.get(it).toAbsolutePath().normalize() }
        .firstOrNull(Files::isDirectory)

    private fun Path.isImageFile(): Boolean = extension() in IMAGE_EXTENSIONS

    private fun Path.extension(): String = fileName.toString()
        .substringAfterLast('.', missingDelimiterValue = "")
        .lowercase()

    private fun Image.contentTypeFromExtension(): String = when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "application/octet-stream"
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun at(
        dayOffset: Long,
        hour: Int,
        minute: Int,
    ): LocalDateTime = MOCK_START_DATE
        .plusDays(dayOffset)
        .atTime(hour, minute)

    private companion object {
        const val MOCK_ACTIVITY_URL = "/activities/2026-summer-reception"
        const val MOCK_IMAGE_USAGE_TYPE = "mock"

        val MOCK_START_DATE: LocalDate = LocalDate.of(2026, 6, 18)
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
        val MOCK_IMAGE_SOURCE_DIRS = listOf(
            "image",
            "images",
            "src/main/kotlin/top/foxball/receptionbackendsystem/image",
        )

        val COLOR_TAGS = listOf(
            ColorTagSeed("重要", "#D92D20"),
            ColorTagSeed("接待", "#1570EF"),
            ColorTagSeed("考察", "#039855"),
            ColorTagSeed("住宿", "#7F56D9"),
            ColorTagSeed("用餐", "#DC6803"),
            ColorTagSeed("交通", "#0086C9"),
        )

        val PERSON_SEEDS = listOf(
            PersonSeed("张明", "省发改委", "张主任", "13900021001"),
            PersonSeed("李雪", "市投资促进局", "李处", "13900021002"),
            PersonSeed("王启航", "产业园管委会", "王工", "13900021003"),
            PersonSeed("孙雅", "城市更新公司", "孙经理", "13900021004"),
            PersonSeed("陈佳", "接待办", "陈专员", "13900020001"),
            PersonSeed("刘海", "会务公司", "刘主管", "13900020002"),
            PersonSeed("周宁", "宣传部", "周老师", "13900020003"),
            PersonSeed("吴越", "后勤保障组", "吴专员", "13900020004"),
            PersonSeed("郑桐", "项目服务中心", "郑经理", "13900020005"),
            PersonSeed("何林", "安保联络组", "何队", "13900020006"),
        )

        val PERSON_SEEDS_BY_NAME: Map<String, PersonSeed> = PERSON_SEEDS.associateBy { it.name }
    }
}

private data class ColorTagSeed(
    val name: String,
    val color: String,
)

private data class PersonSeed(
    val name: String,
    val unit: String,
    val nickName: String,
    val phone: String,
) {
    fun toPerson(activity: Activities? = null): Person = Person(
        activity = activity,
        name = name,
        unit = unit,
        nickName = nickName,
    )
}
