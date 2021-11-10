package io.github.siyual_park.search.finder

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.repository.r2dbc.CachedR2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

open class R2dbcFinder<T : Cloneable<T>, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    protected val cachedRepository: CachedR2DBCRepository<T, ID> = CachedR2DBCRepository.of(repository)
) : Finder<T, ID>(repository, cachedRepository) {
    fun findAll(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null,
        cache: Boolean = false
    ): Flow<T> {
        if (cache) {
            return cachedRepository.findAll(criteria, limit, offset, sort)
        }

        return repository.findAll(criteria, limit, offset, sort)
    }

    suspend fun findOneOrFail(criteria: CriteriaDefinition, cache: Boolean = false): T {
        if (cache) {
            return cachedRepository.findOneOrFail(criteria)
        }

        return repository.findOneOrFail(criteria)
    }

    suspend fun findOne(criteria: CriteriaDefinition, cache: Boolean = false): T? {
        if (cache) {
            return cachedRepository.findOne(criteria)
        }

        return repository.findOne(criteria)
    }
}
