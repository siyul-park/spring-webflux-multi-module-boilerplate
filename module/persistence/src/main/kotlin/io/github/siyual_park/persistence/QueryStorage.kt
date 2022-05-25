package io.github.siyual_park.persistence

import io.github.siyual_park.data.criteria.Criteria
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort

interface QueryStorage<T : Any, ID : Any> : Storage<T, ID> {
    suspend fun load(criteria: Criteria): T?
    fun load(
        criteria: Criteria? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun count(criteria: Criteria? = null): Long
}

suspend fun <T : Any, ID : Any> QueryStorage<T, ID>.loadOrFail(criteria: Criteria): T {
    return load(criteria) ?: throw EmptyResultDataAccessException(1)
}
