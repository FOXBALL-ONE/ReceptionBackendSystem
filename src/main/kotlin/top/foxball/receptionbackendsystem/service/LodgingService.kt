package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Lodging
import top.foxball.receptionbackendsystem.datasource.jdbc.ColorTag

/**
 * 住宿安排业务服务契约。
 *
 * 为 [Lodging] 提供按活动维度查询及整体覆盖保存住宿记录。
 * 住宿记录通过颜色标签分组，因此整体保存时需同时维护关联的 [ColorTag]。
 */
interface LodgingService : ActivityEntityService<Lodging, Int> {
    /**
     * 整体覆盖保存活动下的住宿安排。
     *
     * 以 [activityId] 为维度，先保存 [colorTags]（住宿分类标签），
     * 再以更新后的标签覆盖保存 [lodgings] 列表，返回最终落库的标签与住宿记录。
     */
    fun saveByActivity(activityId: Long, colorTags: List<ColorTag>, lodgings: List<Lodging>): LodgingSaveResult
}

/** 住宿整体保存结果，同时回传最终落库的颜色标签与住宿记录。 */
data class LodgingSaveResult(
    val colorTags: List<ColorTag>,
    val lodgings: List<Lodging>,
)
