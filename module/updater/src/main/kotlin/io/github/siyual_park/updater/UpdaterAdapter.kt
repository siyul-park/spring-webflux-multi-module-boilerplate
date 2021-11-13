package io.github.siyual_park.updater

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.findByIdOrFail
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow

class UpdaterAdapter<T : Cloneable<T>, ID : Any>(
    private val repository: Repository<T, ID>,
    private val eventPublisher: EventPublisher,
) : Updater<T, ID> {
    override suspend fun updateById(id: ID, patch: Patch<T>): T {
        return updateById(id, patch.async())
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T {
        return repository.updateById(id, patch)
            ?.also { eventPublisher.publish(AfterSaveEvent(it)) }
            ?: repository.findByIdOrFail(id)
    }

    override suspend fun update(entity: T): T {
        return repository.update(entity)
            ?.also { eventPublisher.publish(AfterSaveEvent(it)) }
            ?: entity
    }

    override suspend fun update(entity: T, patch: Patch<T>): T {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T {
        return repository.update(entity, patch)
            ?.also { eventPublisher.publish(AfterSaveEvent(it)) }
            ?: entity
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T> {
        return updateAllById(ids, patch.async())
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T> {
        return flow {
            repository.updateAllById(ids, patch)
                .collectIndexed { i, it ->
                    val entity = it?.also { eventPublisher.publish(AfterSaveEvent(it)) }
                        ?: repository.findByIdOrFail(ids.elementAt(i))
                    emit(entity)
                }
        }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T> {
        return flow {
            repository.updateAll(entity)
                .collectIndexed { i, it ->
                    emit(it?.also { eventPublisher.publish(AfterSaveEvent(it)) } ?: entity.elementAt(i))
                }
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T> {
        return flow {
            repository.updateAll(entity, patch)
                .collectIndexed { i, it ->
                    emit(it?.also { eventPublisher.publish(AfterSaveEvent(it)) } ?: entity.elementAt(i))
                }
        }
    }
}
