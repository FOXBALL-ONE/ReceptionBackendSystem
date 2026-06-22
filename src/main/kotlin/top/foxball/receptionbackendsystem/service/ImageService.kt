package top.foxball.receptionbackendsystem.service

import top.foxball.receptionbackendsystem.datasource.jdbc.Image

interface ImageService : ActivityEntityService<Image, Long> {
    fun findByActivityIdAndIsDeletedFalse(activityId: Int): List<Image>

    fun findBySha256AndIsDeletedFalse(sha256: String): Image?
}
