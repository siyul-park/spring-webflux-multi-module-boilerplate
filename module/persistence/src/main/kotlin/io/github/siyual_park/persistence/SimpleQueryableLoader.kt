package io.github.siyual_park.persistence

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.transaction.SuspendTransactionContextHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort

class SimpleQueryableLoader<T : Any, ID : Any, P : Persistence<T, ID>>(
    private val repository: QueryableRepository<T, ID>,
    private val singleMapper: suspend (T) -> P,
    multiMapper: (suspend (Collection<T>) -> Collection<P>)? = null
) : QueryableLoader<P, ID> {
    private val multiMapper = multiMapper ?: { it.map { singleMapper(it) } }

    override suspend fun load(id: ID): P? {
        return repository.findById(id)
            ?.let { singleMapper(it) }
            ?.also { it.link() }
    }

    override suspend fun load(criteria: Criteria): P? {
        return repository.findOne(criteria)
            ?.let { singleMapper(it) }
            ?.also { it.link() }
    }

    override fun load(ids: Iterable<ID>): Flow<P> {
        return flow {
            val context = SuspendTransactionContextHolder.getContext()

            multiMapper(repository.findAllById(ids).toList())
                .onEach { if (context != null) it.link() }
                .let { emitAll(it.asFlow()) }
        }
    }

    override fun load(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<P> {
        return flow {
            val context = SuspendTransactionContextHolder.getContext()
            multiMapper(repository.findAll(criteria, limit, offset, sort).toList())
                .onEach { if (context != null) it.link() }
                .let { emitAll(it.asFlow()) }
        }
    }

    override suspend fun count(criteria: Criteria?): Long {
        return repository.count(criteria)
    }

    override suspend fun count(): Long {
        return repository.count()
    }
}
