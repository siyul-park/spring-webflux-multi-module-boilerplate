package io.github.siyual_park.reader.finder

import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow

class FinderAdapter<T : Any, ID : Any>(
    private val repository: Repository<T, ID>
) : Finder<T, ID> {
    override suspend fun findById(id: ID): T? {
        return repository.findById(id)
    }

    override fun findAll(): Flow<T> {
        return repository.findAll()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return repository.findAllById(ids)
    }
}
