package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

interface ColorTagService : ActivityEntityService<ColorTag, Int> {
    fun findByActivityIdAndName(activityId: Long, name: String): ColorTag?

    fun existsByActivityIdAndName(activityId: Long, name: String): Boolean
}
