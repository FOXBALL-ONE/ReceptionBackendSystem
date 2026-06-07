package top.foxball.receptionbackendsystem.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.Image
import top.foxball.receptionbackendsystem.datasource.jdbc.ImageRepository
import top.foxball.receptionbackendsystem.handlder.ResourceNotFoundException
import top.foxball.receptionbackendsystem.service.ImageService

/**
 * 图片文件元数据业务服务实现。
 */
@Service
@Transactional
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
) : ImageService {

    /** 查询全部未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findAll(): List<Image> = imageRepository.findByIsDeletedFalseOrderByCreatedAtDesc()

    /** 根据主键查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findById(id: Long): Image = imageRepository.findById(id)
        .filter { !it.isDeleted }
        .orElseThrow { ResourceNotFoundException("图片不存在：$id") }

    /** 根据对象键查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findByObjectKey(objectKey: String): Image = imageRepository.findByObjectKeyAndIsDeletedFalse(objectKey)
        ?: throw ResourceNotFoundException("图片不存在：$objectKey")

    /** 根据 SHA-256 摘要查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findBySha256(sha256: String): Image = imageRepository.findBySha256AndIsDeletedFalse(sha256)
        ?: throw ResourceNotFoundException("图片不存在：$sha256")

    /** 根据用途类型查询未删除图片元数据。 */
    @Transactional(readOnly = true)
    override fun findByUsageType(usageType: String): List<Image> =
        imageRepository.findByUsageTypeAndIsDeletedFalseOrderByCreatedAtDesc(usageType)

    /** 创建图片元数据。 */
    override fun create(image: Image): Image {
        image.id = null
        image.isDeleted = false
        return imageRepository.save(image)
    }

    /** 更新图片元数据，保留创建时间和删除状态。 */
    override fun update(id: Long, image: Image): Image {
        val existing = findById(id)

        image.id = id
        image.createdAt = existing.createdAt
        image.isDeleted = existing.isDeleted
        return imageRepository.save(image)
    }

    /** 软删除图片元数据。 */
    override fun delete(id: Long) {
        val image = findById(id)
        image.isDeleted = true
        imageRepository.save(image)
    }
}
