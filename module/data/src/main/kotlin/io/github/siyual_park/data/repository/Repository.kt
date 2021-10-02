package io.github.siyual_park.data.repository

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.dao.EmptyResultDataAccessException

interface Repository<T : Any, ID : Any> {
    suspend fun create(entity: T): T

    fun createAll(entities: Iterable<T>): Flow<T>

    suspend fun existsById(id: ID): Boolean

    suspend fun findById(id: ID): T?

    fun findAll(): Flow<T>

    fun findAllById(ids: Iterable<ID>): Flow<T>

    suspend fun updateById(id: ID, patch: Patch<T>): T?
    suspend fun updateById(id: ID, patch: AsyncPatch<T>): T?

    suspend fun update(entity: T): T?
    suspend fun update(entity: T, patch: Patch<T>): T?
    suspend fun update(entity: T, patch: AsyncPatch<T>): T?

    fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?>
    fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?>

    fun updateAll(entity: Iterable<T>): Flow<T?>
    fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?>
    fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?>

    suspend fun count(): Long

    suspend fun deleteById(id: ID)

    suspend fun delete(entity: T)

    suspend fun deleteAllById(ids: Iterable<ID>)

    suspend fun deleteAll(entities: Iterable<T>)
    suspend fun deleteAll()
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.findByIdOrFail(id: ID): T {
    return findById(id) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.updateByIdOrFail(id: ID, patch: Patch<T>): T {
    return updateById(id, patch) ?: throw EmptyResultDataAccessException(1)
}
suspend fun <T : Any, ID : Any> Repository<T, ID>.updateByIdOrFail(id: ID, patch: AsyncPatch<T>): T {
    return updateById(id, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.updateOrFail(entity: T): T {
    return update(entity) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.updateOrFail(entity: T, patch: Patch<T>): T {
    return update(entity, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.updateOrFail(entity: T, patch: AsyncPatch<T>): T {
    return update(entity, patch) ?: throw EmptyResultDataAccessException(1)
}

fun <T : Any, ID : Any> Repository<T, ID>.createAll(entities: Flow<T>): Flow<T> {
    return flow { emitAll(createAll(entities.toList())) }
}

fun <T : Any, ID : Any> Repository<T, ID>.findAllById(ids: Flow<ID>): Flow<T> {
    return flow { emitAll(findAllById(ids.toList())) }
}

fun <T : Any, ID : Any> Repository<T, ID>.updateAllById(ids: Flow<ID>, patch: Patch<T>): Flow<T?> {
    return flow { emitAll(updateAllById(ids.toList(), patch)) }
}

fun <T : Any, ID : Any> Repository<T, ID>.updateAllById(ids: Flow<ID>, patch: AsyncPatch<T>): Flow<T?> {
    return flow { emitAll(updateAllById(ids.toList(), patch)) }
}

fun <T : Any, ID : Any> Repository<T, ID>.updateAll(entity: Flow<T>): Flow<T?> {
    return flow { emitAll(updateAll(entity.toList())) }
}

fun <T : Any, ID : Any> Repository<T, ID>.updateAll(entity: Flow<T>, patch: Patch<T>): Flow<T?> {
    return flow { emitAll(updateAll(entity.toList(), patch)) }
}

fun <T : Any, ID : Any> Repository<T, ID>.updateAll(entity: Flow<T>, patch: AsyncPatch<T>): Flow<T?> {
    return flow { emitAll(updateAll(entity.toList(), patch)) }
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.deleteAllById(ids: Flow<ID>) {
    deleteAllById(ids.toList())
}

suspend fun <T : Any, ID : Any> Repository<T, ID>.deleteAll(entities: Flow<T>) {
    deleteAll(entities.toList())
}
