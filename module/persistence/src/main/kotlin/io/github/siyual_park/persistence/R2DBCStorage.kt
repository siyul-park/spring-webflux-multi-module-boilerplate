package io.github.siyual_park.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

interface R2DBCStorage<T : Any, ID : Any> : Storage<T, ID> {
    suspend fun load(criteria: CriteriaDefinition): T?
    fun load(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun count(criteria: CriteriaDefinition? = null): Long
}

suspend fun <T : Any, ID : Any> R2DBCStorage<T, ID>.loadOrFail(criteria: CriteriaDefinition): T {
    return load(criteria) ?: throw EmptyResultDataAccessException(1)
}
