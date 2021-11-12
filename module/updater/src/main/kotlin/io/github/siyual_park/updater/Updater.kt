package io.github.siyual_park.updater

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException

interface Updater<T : Any, ID : Any> {
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
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateByIdOrFail(id: ID, patch: Patch<T>): T {
    return updateById(id, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateByIdOrFail(id: ID, patch: AsyncPatch<T>): T {
    return updateById(id, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateByIdOrFail(id: ID, patch: (entity: T) -> Unit): T {
    return updateByIdOrFail(id, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateOrFail(entity: T): T {
    return update(entity) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateOrFail(entity: T, patch: Patch<T>): T {
    return update(entity, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateOrFail(entity: T, patch: AsyncPatch<T>): T {
    return update(entity, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateOrFail(entity: T, patch: (entity: T) -> Unit): T {
    return updateOrFail(entity, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.update(entity: T, patch: (entity: T) -> Unit): T? {
    return update(entity, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> Updater<T, ID>.updateById(id: ID, patch: (entity: T) -> Unit): T? {
    return updateById(id, Patch.with(patch))
}
