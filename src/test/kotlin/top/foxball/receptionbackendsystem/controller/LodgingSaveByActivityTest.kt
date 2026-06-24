package top.foxball.receptionbackendsystem.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ActivitiesRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTagRepository
import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.Person
import top.foxball.receptionbackendsystem.service.LodgingService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
class LodgingSaveByActivityTest {
    @Autowired
    private lateinit var activitiesRepository: ActivitiesRepository

    @Autowired
    private lateinit var lodgingService: LodgingService

    @Autowired
    private lateinit var colorTagRepository: ColorTagRepository

    @Test
    @Transactional
    fun `save by activity replaces lodgings and color tags`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val colorTag = ColorTag(name = "接口住宿组", color = "#123456", type = ColorTag.TYPE_LODGING)

        val savedResult = lodgingService.saveByActivity(
            activityId = activityId,
            colorTags = listOf(colorTag),
            lodgings = listOf(
                Lodging(
                    roomNumber = "T-1001",
                    person = Person(name = "接口住宿人员", unit = "接口测试单位", nickName = "住宿测试"),
                    colorTag = colorTag,
                ),
            ),
        )

        assertEquals(1, savedResult.colorTags.size)
        assertEquals(1, savedResult.lodgings.size)
        assertEquals(activityId, savedResult.colorTags.first().activity?.id)
        assertEquals("接口住宿组", savedResult.colorTags.first().name)
        assertEquals(ColorTag.TYPE_LODGING, savedResult.colorTags.first().type)
        assertEquals(savedResult.colorTags.first().id, savedResult.lodgings.first().colorTag?.id)
        assertEquals("接口住宿人员", savedResult.lodgings.first().person?.name)

        val colorTagId = assertNotNull(savedResult.colorTags.first().id)
        val lodgingId = assertNotNull(savedResult.lodgings.first().id)
        val updatedResult = lodgingService.saveByActivity(
            activityId = activityId,
            colorTags = listOf(
                ColorTag(id = colorTagId, name = "接口住宿组-更新", color = "#654321", type = ColorTag.TYPE_LODGING),
            ),
            lodgings = listOf(
                Lodging(
                    id = lodgingId,
                    roomNumber = "T-2002",
                    person = Person(name = "接口住宿人员-更新", unit = "接口测试单位-更新", nickName = "住宿更新"),
                    colorTag = ColorTag(id = colorTagId),
                ),
            ),
        )

        assertEquals(colorTagId, updatedResult.colorTags.first().id)
        assertEquals(lodgingId, updatedResult.lodgings.first().id)
        assertEquals("接口住宿组-更新", updatedResult.colorTags.first().name)
        assertEquals(ColorTag.TYPE_LODGING, updatedResult.colorTags.first().type)
        assertEquals("T-2002", updatedResult.lodgings.first().roomNumber)
        assertEquals(colorTagId, updatedResult.lodgings.first().colorTag?.id)

        val reloadedLodgings = lodgingService.findByActivityId(activityId)
        assertEquals(1, reloadedLodgings.size)
        assertEquals("T-2002", reloadedLodgings.first().roomNumber)
    }

    @Test
    @Transactional
    fun `save by activity reuses lodging color tag when request contains person color tag`() {
        val activity = activitiesRepository.findAll().first()
        val activityId = assertNotNull(activity.id)
        val personColorTag = colorTagRepository.saveAndFlush(
            ColorTag(
                activity = activity,
                name = "reuse-color-tag",
                color = "#ABCDEF",
                type = ColorTag.TYPE_PERSON,
            ),
        )
        val lodgingColorTag = colorTagRepository.saveAndFlush(
            ColorTag(
                activity = activity,
                name = "reuse-color-tag",
                color = "#ABCDEF",
                type = ColorTag.TYPE_LODGING,
            ),
        )
        activity.colorTagList.add(personColorTag)
        activity.colorTagList.add(lodgingColorTag)
        val personColorTagId = assertNotNull(personColorTag.id)
        val lodgingColorTagId = assertNotNull(lodgingColorTag.id)

        val savedResult = lodgingService.saveByActivity(
            activityId = activityId,
            colorTags = listOf(
                ColorTag(
                    id = personColorTagId,
                    name = personColorTag.name,
                    color = personColorTag.color,
                    type = personColorTag.type,
                ),
            ),
            lodgings = listOf(
                Lodging(
                    roomNumber = "T-3003",
                    person = Person(name = "lodging color tag reuse"),
                    colorTag = ColorTag(id = personColorTagId),
                ),
            ),
        )

        assertEquals(1, savedResult.colorTags.size)
        assertEquals(lodgingColorTagId, savedResult.colorTags.first().id)
        assertEquals(ColorTag.TYPE_LODGING, savedResult.colorTags.first().type)
        assertEquals(lodgingColorTagId, savedResult.lodgings.first().colorTag?.id)
    }
}
