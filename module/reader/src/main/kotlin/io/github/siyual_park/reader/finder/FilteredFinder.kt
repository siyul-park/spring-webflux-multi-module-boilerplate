package io.github.siyual_park.reader.finder

import io.github.siyual_park.data.IdEntity
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition

open class FilteredFinder<T : IdEntity<T, ID>, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    private val filter: CriteriaDefinition? = null
) : Finder<T, ID> {
    override suspend fun findById(id: ID): T? {
        return repository.findOne(applyFilter(where(IdEntity<T, ID>::id).`is`(id)))
    }

    override fun findAll(): Flow<T> {
        return repository.findAll(filter)
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return repository.findAll(applyFilter(where(IdEntity<T, ID>::id).`in`(ids.toList())))
    }

    protected fun applyFilter(criteria: Criteria): Criteria {
        return if (filter == null) {
            criteria
        } else {
            criteria.and(filter)
        }
    }
}
