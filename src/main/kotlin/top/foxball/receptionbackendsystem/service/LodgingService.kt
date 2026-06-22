package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

interface LodgingService : ActivityEntityService<Lodging, Int> {
    fun saveByActivity(activityId: Long, colorTags: List<ColorTag>, lodgings: List<Lodging>): LodgingSaveResult
}

data class LodgingSaveResult(
    val colorTags: List<ColorTag>,
    val lodgings: List<Lodging>,
)
