package io.github.siyual_park.search.finder

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.findByIdOrFail
import kotlinx.coroutines.flow.Flow

open class Finder<T : Cloneable<T>, ID : Any>(
    private val repository: Repository<T, ID>,
    private val cachedRepository: Repository<T, ID>
) {
    suspend fun findById(id: ID, cache: Boolean = false): T? {
        if (cache) {
            return cachedRepository.findById(id)
        }

        return repository.findById(id)
    }

    suspend fun findByIdOrFail(id: ID, cache: Boolean = false): T {
        if (cache) {
            return cachedRepository.findByIdOrFail(id)
        }

        return repository.findByIdOrFail(id)
    }

    fun findAll(cache: Boolean = false): Flow<T> {
        if (cache) {
            return cachedRepository.findAll()
        }

        return repository.findAll()
    }

    fun findAllById(ids: Iterable<ID>, cache: Boolean = false): Flow<T> {
        if (cache) {
            return cachedRepository.findAllById(ids)
        }

        return repository.findAllById(ids)
    }
}
