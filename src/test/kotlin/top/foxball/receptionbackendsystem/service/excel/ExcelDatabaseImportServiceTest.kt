package top.foxball.receptionbackendsystem.service.excel

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.write.metadata.WriteSheet
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import top.foxball.receptionbackendsystem.datasource.excel.AttendanceGuidelinesItem
import top.foxball.receptionbackendsystem.datasource.excel.CarExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.InspectionPointsItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingItem
import top.foxball.receptionbackendsystem.datasource.excel.LodgingStaffExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.MealItem
import top.foxball.receptionbackendsystem.datasource.excel.OverviewOfTheCityAndCountyExcelRow
import top.foxball.receptionbackendsystem.datasource.excel.PersonnelItem
import top.foxball.receptionbackendsystem.datasource.excel.ScheduleExcelRow
import top.foxball.receptionbackendsystem.datasource.jdbc.Activities
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExcelDatabaseImportServiceTest {

    @Test
    fun `imports personnel and lodging color tags from inspection teams`() {
        val repository = InMemoryActivitiesRepository()
        val service = ExcelDatabaseImportService(
            activitiesRepository = repository.proxy(),
            personnelExcelService = PersonnelExcelService(),
            carExcelService = CarExcelService(),
            mealExcelService = MealExcelService(),
            lodgingExcelService = LodgingExcelService(),
            lodgingStaffExcelService = LodgingStaffExcelService(),
            scheduleExcelService = ScheduleExcelService(),
            inspectionPointsExcelService = InspectionPointsExcelService(),
            attendanceGuidelinesExcelService = AttendanceGuidelinesExcelService(),
            overviewExcelService = OverviewOfTheCityAndCountyExcelService(),
        )

        service.importExcel(
            MockMultipartFile(
                "file",
                "import.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                workbookBytes(),
            ),
            ExcelDatabaseImportOptions(url = "unit-test-import", replaceExistingByUrl = true),
        )

        val activity = assertNotNull(repository.savedActivity)
        val personTags = activity.colorTagList.filter { it.type == ColorTag.TYPE_PERSON }
        val lodgingTags = activity.colorTagList.filter { it.type == ColorTag.TYPE_LODGING }

        assertEquals(setOf("一组", "二组"), personTags.mapNotNull { it.name }.toSet())
        assertEquals(setOf("一组", "二组"), lodgingTags.mapNotNull { it.name }.toSet())
        assertTrue(activity.colorTagList.none { it.name == "人员旧类别" || it.name == "住宿旧类别" })
        assertTrue(activity.colorTagList.all { it.color?.matches(HEX_COLOR) == true })

        val personColorByTeam = personTags.associate { it.name to it.color }
        val lodgingColorByTeam = lodgingTags.associate { it.name to it.color }
        assertEquals(personColorByTeam["一组"], lodgingColorByTeam["一组"])
        assertEquals(personColorByTeam["二组"], lodgingColorByTeam["二组"])

        val teamIdsByName = activity.inspectionTeamItemList.associate { it.name to it.id }
        val zhangSan = assertNotNull(activity.personList.firstOrNull { it.name == "张三" })
        val zhangSanLodging = assertNotNull(activity.hostedList.firstOrNull { it.person?.name == "张三" })
        assertEquals(2, activity.hostedList.size)
        assertTrue(activity.hostedList.none { it.roomNumber == "999" })
        assertEquals("一组", zhangSan.colorTag?.name)
        assertEquals("一组", zhangSanLodging.colorTag?.name)
        assertEquals(teamIdsByName["一组"], zhangSan.inspectionTeamItemId)
        assertEquals(teamIdsByName["一组"], zhangSanLodging.person?.inspectionTeamItemId)
    }

    private fun workbookBytes(): ByteArray {
        val out = ByteArrayOutputStream()
        EasyExcel.write(out).build().use { writer ->
            writer.write(
                listOf(
                    PersonnelItem(id = 1, name = "张三", unit = "单位A", position = "人员旧类别", inspectionTeam = "一组"),
                    PersonnelItem(id = 2, name = "李四", unit = "单位B", position = "人员旧类别", inspectionTeam = "二组"),
                ),
                sheet(0, "人员名单", PersonnelItem::class.java),
            )
            writer.write(emptyList<CarExcelRow>(), sheet(1, "乘车安排", CarExcelRow::class.java))
            writer.write(emptyList<MealItem>(), sheet(2, "用餐安排", MealItem::class.java))
            writer.write(
                listOf(
                    LodgingItem(name = "张三", unit = "单位A", roomNumber = "房号101", position = "住宿旧类别"),
                    LodgingItem(name = "李四", unit = "单位B", roomNumber = "房号102", position = "住宿旧类别"),
                    LodgingItem(name = "张三", unit = "单位A", roomNumber = null, position = "住宿旧类别"),
                    LodgingItem(name = null, unit = "单位C", roomNumber = "房号999", position = "住宿旧类别"),
                ),
                sheet(3, "住宿安排", LodgingItem::class.java),
            )
            writer.write(emptyList<LodgingStaffExcelRow>(), sheet(4, "工作人员", LodgingStaffExcelRow::class.java))
            writer.write(
                listOf(
                    ScheduleExcelRow(
                        date = "5月28日",
                        inspectionTeam = "一组",
                        line = "酒店 → 考察点A",
                        time = "08:00",
                        context = "出发",
                        location = "酒店",
                    ),
                    ScheduleExcelRow(
                        date = "5月28日",
                        inspectionTeam = "二组",
                        line = "酒店 → 考察点B",
                        time = "08:30",
                        context = "出发",
                        location = "酒店",
                    ),
                ),
                sheet(5, "日程安排", ScheduleExcelRow::class.java),
            )
            writer.write(emptyList<InspectionPointsItem>(), sheet(6, "考察点", InspectionPointsItem::class.java))
            writer.write(emptyList<AttendanceGuidelinesItem>(), sheet(7, "参会须知", AttendanceGuidelinesItem::class.java))
            writer.write(
                emptyList<OverviewOfTheCityAndCountyExcelRow>(),
                sheet(8, "市县概况", OverviewOfTheCityAndCountyExcelRow::class.java),
            )
        }
        return out.toByteArray()
    }

    private fun sheet(index: Int, name: String, head: Class<*>): WriteSheet =
        EasyExcel.writerSheet(index, name).head(head).build()

    private class InMemoryActivitiesRepository : InvocationHandler {
        var savedActivity: Activities? = null
            private set

        private var nextActivityId = 1L
        private var nextTeamId = 1L
        private var nextColorTagId = 1

        fun proxy(): ActivitiesRepository =
            Proxy.newProxyInstance(
                ActivitiesRepository::class.java.classLoader,
                arrayOf(ActivitiesRepository::class.java),
                this,
            ) as ActivitiesRepository

        override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? =
            when (method.name) {
                "saveAndFlush" -> (args?.firstOrNull() as Activities).also { activity ->
                    assignIds(activity)
                    savedActivity = activity
                }

                "findByUrl" -> null
                "delete", "flush" -> null
                "toString" -> "InMemoryActivitiesRepository"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> defaultValue(method.returnType)
            }

        private fun assignIds(activity: Activities) {
            if (activity.id == null) {
                activity.id = nextActivityId++
            }
            activity.inspectionTeamItemList.forEach { team ->
                if (team.id == null) {
                    team.id = nextTeamId++
                }
            }
            activity.colorTagList.forEach { colorTag ->
                if (colorTag.id == null) {
                    colorTag.id = nextColorTagId++
                }
            }
        }

        private fun defaultValue(returnType: Class<*>): Any? =
            when {
                returnType == java.lang.Boolean.TYPE -> false
                returnType == java.lang.Integer.TYPE -> 0
                returnType == java.lang.Long.TYPE -> 0L
                returnType == java.lang.Void.TYPE -> null
                Optional::class.java.isAssignableFrom(returnType) -> Optional.empty<Any>()
                List::class.java.isAssignableFrom(returnType) -> emptyList<Any>()
                else -> null
            }
    }

    private companion object {
        val HEX_COLOR = Regex("^#[0-9A-F]{6}$")
    }
}
