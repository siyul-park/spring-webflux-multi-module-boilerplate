package io.github.siyual_park.persistence

import io.github.siyual_park.data.repository.mongo.MongoRepository
import io.github.siyual_park.data.transaction.currentContextOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.CriteriaDefinition

class SimpleMongoStorage<T : Any, ID : Any, P : Persistence<T, ID>>(
    private val repository: MongoRepository<T, ID>,
    private val mapper: suspend (T) -> P
) : MongoStorage<P, ID> {
    override suspend fun load(id: ID): P? {
        return repository.findById(id)
            ?.let { mapper(it) }
            ?.also { it.link() }
    }

    override suspend fun load(criteria: CriteriaDefinition): P? {
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

    override fun load(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<P> {
        return flow {
            val context = currentContextOrNull()
            repository.findAll(criteria, limit, offset, sort)
                .map { mapper(it) }
                .onEach { if (context != null) it.link() }
                .collect { emit(it) }
        }
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return repository.count(criteria)
    }

    override suspend fun count(): Long {
        return repository.count()
    }
}
