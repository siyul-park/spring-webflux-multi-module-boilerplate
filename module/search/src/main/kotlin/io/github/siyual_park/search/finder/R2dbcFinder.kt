package io.github.siyual_park.search.finder

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.repository.cache.CachedRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCCachedRepository
import io.github.siyual_park.data.repository.r2dbc.R2DBCRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

open class R2dbcFinder<T : Cloneable<T>, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    cachedRepository: CachedRepository<T, ID> = R2DBCCachedRepository(repository)
) : Finder<T, ID>(repository, cachedRepository) {
    fun findAll(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T> {
        return repository.findAll(criteria, limit, offset, sort)
    }

    suspend fun findOneOrFail(criteria: CriteriaDefinition): T {
        return repository.findOneOrFail(criteria)
    }

    suspend fun findOne(criteria: CriteriaDefinition): T? {
        return repository.findOne(criteria)
    }
}
