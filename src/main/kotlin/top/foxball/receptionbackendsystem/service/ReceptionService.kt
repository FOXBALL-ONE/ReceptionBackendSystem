package top.foxball.receptionbackendsystem.service

interface ReceptionService<T : Any, ID : Any> {
    fun saveOne(entity: T): T

    fun saveBatch(entities: Iterable<T>): List<T>

    fun updateOne(entity: T): T

    fun updateBatch(entities: Iterable<T>): List<T>

    fun deleteOne(entity: T)

    fun deleteBatch(entities: Iterable<T>)

    fun findEntityById(id: ID): T?
}
