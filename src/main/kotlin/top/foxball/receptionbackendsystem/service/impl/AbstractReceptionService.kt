package top.foxball.receptionbackendsystem.service.impl

import org.springframework.transaction.annotation.Transactional
import top.foxball.receptionbackendsystem.datasource.jdbc.ReceptionRepository
import top.foxball.receptionbackendsystem.service.ReceptionService

abstract class AbstractReceptionService<T : Any, ID : Any>(
    private val repository: ReceptionRepository<T, ID>,
) : ReceptionService<T, ID> {
    @Transactional
    override fun saveOne(entity: T): T = repository.saveOne(entity)

    @Transactional
    override fun saveBatch(entities: Iterable<T>): List<T> = repository.saveBatch(entities)

    @Transactional
    override fun updateOne(entity: T): T = repository.updateOne(entity)

    @Transactional
    override fun updateBatch(entities: Iterable<T>): List<T> = repository.updateBatch(entities)

    @Transactional
    override fun deleteOne(entity: T) {
        repository.deleteOne(entity)
    }

    @Transactional
    override fun deleteBatch(entities: Iterable<T>) {
        repository.deleteBatch(entities)
    }

    @Transactional(readOnly = true)
    override fun findEntityById(id: ID): T? = repository.findEntityById(id)
}
