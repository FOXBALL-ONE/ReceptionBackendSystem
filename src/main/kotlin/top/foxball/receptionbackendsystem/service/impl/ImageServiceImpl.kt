package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.ImageRepository
import top.foxball.receptionbackendsystem.service.ImageService

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
) : AbstractReceptionService<Image, Long>(imageRepository), ImageService {
    @Transactional(readOnly = true)
    override fun findByActivityId(activityId: Int): List<Image> =
        imageRepository.findByActivityId(activityId)

    @Transactional(readOnly = true)
    override fun findByActivityIdAndIsDeletedFalse(activityId: Int): List<Image> =
        imageRepository.findByActivityIdAndIsDeletedFalse(activityId)

    @Transactional(readOnly = true)
    override fun findBySha256AndIsDeletedFalse(sha256: String): Image? =
        imageRepository.findBySha256AndIsDeletedFalse(sha256)
}
