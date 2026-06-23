package top.foxball.receptionbackendsystem.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.PersonService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class PersonSaveByActivityTest {
    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Autowired
    private lateinit var personService: PersonService

    @Test
    @Transactional
    fun `save by activity replaces persons`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val colorTag = assertNotNull(activity.colorTagList.firstOrNull { it.type == ColorTag.TYPE_PERSON })

        val savedPersons = personService.saveByActivity(
            activityId = activityId,
            persons = listOf(
                Person(
                    name = "接口测试人员",
                    unit = "接口测试单位",
                    nickName = "测试展示名",
                    colorTag = colorTag,
                    inspectionTeamItemId = 1L,
                ),
            ),
        )

        assertEquals(1, savedPersons.size)
        assertEquals(activityId, savedPersons.first().activity?.id)
        assertEquals("接口测试人员", savedPersons.first().name)
        assertEquals(colorTag.id, savedPersons.first().colorTag?.id)

        val personId = assertNotNull(savedPersons.first().id)
        val updatedPersons = personService.saveByActivity(
            activityId = activityId,
            persons = listOf(
                Person(
                    id = personId,
                    name = "接口测试人员-更新",
                    unit = "接口测试单位-更新",
                    nickName = "更新展示名",
                    colorTag = colorTag,
                    inspectionTeamItemId = 2L,
                ),
            ),
        )

        assertEquals(personId, updatedPersons.first().id)
        assertEquals("接口测试人员-更新", updatedPersons.first().name)
        assertEquals("接口测试单位-更新", updatedPersons.first().unit)
        assertEquals(colorTag.id, updatedPersons.first().colorTag?.id)

        val reloadedPersons = personService.findByActivityId(activityId)
        assertEquals(1, reloadedPersons.size)
        assertEquals("接口测试人员-更新", reloadedPersons.first().name)
        assertEquals(colorTag.id, reloadedPersons.first().colorTag?.id)
    }
}
