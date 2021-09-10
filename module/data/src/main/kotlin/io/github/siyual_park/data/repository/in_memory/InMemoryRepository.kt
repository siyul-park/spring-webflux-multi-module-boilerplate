package io.github.siyual_park.data.repository.in_memory

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.in_memory.callback.EntityCallbacks
import io.github.siyual_park.data.repository.in_memory.generator.IdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.data.annotation.Id
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

open class InMemoryRepository<T : Cloneable<T>, ID : Any>(
    clazz: KClass<T>,
    private val idGenerator: IdGenerator<ID>,
    protected var entityCallbacks: EntityCallbacks<T>? = null
) : Repository<T, ID> {
    private val idProperty = clazz.memberProperties
        .filter { it is KMutableProperty<*> }
        .filter { it.annotations.any { it is Id } }
        .first() as KMutableProperty<ID?>

    protected val datasource: ConcurrentHashMap<ID, T> = ConcurrentHashMap()

    override suspend fun create(entity: T): T {
        val id = idGenerator.generate()
        idProperty.setter.call(entity, id)

        val newEntity = entity.clone()
        entityCallbacks?.onCreate(newEntity)
        datasource[id] = newEntity

        return entity
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return entities.asFlow()
            .map { create(it) }
    }

    override suspend fun existsById(id: ID): Boolean {
        return datasource.contains(id)
    }

    override suspend fun findById(id: ID): T? {
        return datasource[id]?.clone()
    }

    override fun findAll(): Flow<T> {
        return datasource.values.asFlow()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return ids.asFlow()
            .map { findById(it) }
            .filterNotNull()
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return updateById(id, patch.async())
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return findById(id)?.let { update(it, patch) }
    }

    override suspend fun update(entity: T): T? {
        return idProperty.getter.call(entity)?.let {
            val origin = datasource[it]
            if (origin != null) {
                val newEntity = entity.clone()
                entityCallbacks?.onUpdate(origin, newEntity)
                datasource[it] = newEntity
            }

            entity
        }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return update(patch.apply(entity))
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return ids.asFlow()
            .map { updateById(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override suspend fun count(): Long {
        return datasource.size.toLong()
    }

    override suspend fun deleteById(id: ID) {
        val origin = datasource[id]
        if (origin != null) {
            entityCallbacks?.onDelete(origin)
            datasource.remove(id)
        }
    }

    override suspend fun delete(entity: T) {
        idProperty.getter.call(entity)?.let { deleteById(it) }
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        ids.forEach { deleteById(it) }
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.forEach { delete(it) }
    }

    override suspend fun deleteAll() {
        datasource.clear()
    }
}
