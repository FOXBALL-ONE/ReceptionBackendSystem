package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

interface ColorTagService : ActivityEntityService<ColorTag, Int> {
    fun saveByActivity(activityId: Long, colorTags: List<ColorTag>): List<ColorTag>

    fun findByActivityIdAndType(activityId: Long, type: String): List<ColorTag>

    fun findByActivityIdAndName(activityId: Long, name: String): ColorTag?

    fun findByActivityIdAndNameAndType(activityId: Long, name: String, type: String): ColorTag?

    fun existsByActivityIdAndName(activityId: Long, name: String): Boolean

    fun existsByActivityIdAndNameAndType(activityId: Long, name: String, type: String): Boolean
}
