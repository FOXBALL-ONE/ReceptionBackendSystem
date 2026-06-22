package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

interface ColorTagService : ActivityEntityService<ColorTag, Int> {
    fun findByActivityIdAndName(activityId: Int, name: String): ColorTag?

    fun existsByActivityIdAndName(activityId: Int, name: String): Boolean
}
