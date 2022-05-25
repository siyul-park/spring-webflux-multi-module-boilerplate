package io.github.siyual_park.persistence

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.transaction.currentContextOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.Sort

class SimpleQueryStorage<T : Any, ID : Any, P : Persistence<T, ID>>(
    private val repository: QueryRepository<T, ID>,
    private val mapper: suspend (T) -> P
) : QueryStorage<P, ID> {
    override suspend fun load(id: ID): P? {
        return repository.findById(id)
            ?.let { mapper(it) }
            ?.also { it.link() }
    }

    override suspend fun load(criteria: Criteria): P? {
        return repository.findOne(criteria)
            ?.let { mapper(it) }
            ?.also { it.link() }
    }

    override fun load(ids: Iterable<ID>): Flow<P> {
        return flow {
            val context = currentContextOrNull()
            repository.findAllById(ids)
                .map { mapper(it) }
                .onEach { if (context != null) it.link() }
                .collect { emit(it) }
        }
    }

    override fun load(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<P> {
        return flow {
            val context = currentContextOrNull()
            repository.findAll(criteria, limit, offset, sort)
                .map { mapper(it) }
                .onEach { if (context != null) it.link() }
                .collect { emit(it) }
        }
    }

    override suspend fun count(criteria: Criteria?): Long {
        return repository.count(criteria)
    }

    override suspend fun count(): Long {
        return repository.count()
    }
}
