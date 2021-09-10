package io.github.siyual_park.data.repository

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.data.annotation.Id
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class InMemoryRepository<T : Any, ID : Any>(
    clazz: KClass<T>,
    private val idGenerator: IdGenerator<ID>
) : Repository<T, ID> {
    val idProperty = clazz.memberProperties
        .filter { it is KMutableProperty<*> }
        .filter { it.annotations.any { it is Id } }
        .first() as KMutableProperty<ID?>

    val datasource: ConcurrentHashMap<ID, T> = ConcurrentHashMap()

    override suspend fun create(entity: T): T {
        val id = idGenerator.generate()
        idProperty.setter.call(entity, id)

        datasource[id] = entity

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
        return datasource[id]
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
            datasource[it] = entity
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
        datasource.remove(id)
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
