package io.github.siyual_park.updater

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import kotlinx.coroutines.flow.Flow

interface Updater<T : Any, ID : Any> {
    suspend fun updateById(id: ID, patch: Patch<T>): T

    suspend fun updateById(id: ID, patch: AsyncPatch<T>): T

    suspend fun update(entity: T): T

    suspend fun update(entity: T, patch: Patch<T>): T

    suspend fun update(entity: T, patch: AsyncPatch<T>): T

    fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T>

    fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T>

    fun updateAll(entity: Iterable<T>): Flow<T>

    fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T>

    fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T>
}
