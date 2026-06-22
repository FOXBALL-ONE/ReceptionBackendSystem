package top.foxball.receptionbackendsystem.datasource.jdbc

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.transaction.annotation.Transactional

/**
 * 接待系统 JPA 仓库基接口。
 *
 * Spring Data JPA 已内置这些能力，这里提供语义化方法名，便于业务层统一调用。
 */
@NoRepositoryBean
interface ReceptionRepository<T : Any, ID : Any> : JpaRepository<T, ID> {
    @Transactional
    fun saveOne(entity: T): T = save(entity)

    @Transactional
    fun saveBatch(entities: Iterable<T>): List<T> = saveAll(entities).toList()

    @Transactional
    fun updateOne(entity: T): T = save(entity)

    @Transactional
    fun updateBatch(entities: Iterable<T>): List<T> = saveAll(entities).toList()

    @Transactional
    fun deleteOne(entity: T) {
        delete(entity)
    }

    @Transactional
    fun deleteBatch(entities: Iterable<T>) {
        deleteAll(entities)
    }

    fun findEntityById(id: ID): T? = findById(id).orElse(null)
}
