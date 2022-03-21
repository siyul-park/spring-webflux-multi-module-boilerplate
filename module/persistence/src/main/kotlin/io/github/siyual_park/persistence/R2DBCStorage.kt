package io.github.siyual_park.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

interface R2DBCStorage<T : Any, ID : Any, P : Persistence<T, ID>> : Storage<T, ID, P> {
    suspend fun load(criteria: CriteriaDefinition): P?
    fun load(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<P>

    suspend fun count(criteria: CriteriaDefinition? = null): Long
}

suspend fun <T : Any, ID : Any, P : Persistence<T, ID>> R2DBCStorage<T, ID, P>.loadOrFail(criteria: CriteriaDefinition): P {
    return load(criteria) ?: throw EmptyResultDataAccessException(1)
}
