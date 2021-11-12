package io.github.siyual_park.updater

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow

class UpdaterAdapter<T : Cloneable<T>, ID : Any>(
    private val repository: Repository<T, ID>
) : Updater<T, ID> {
    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return updateById(id, patch.async())
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return repository.updateById(id, patch)
    }

    override suspend fun update(entity: T): T? {
        return repository.update(entity)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return repository.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAllById(ids, patch)
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return repository.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return repository.updateAll(entity, patch)
    }
}
