package io.github.siyual_park.data.repository

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort

interface QueryRepository<T : Any, ID : Any> : Repository<T, ID> {
    suspend fun exists(criteria: Criteria): Boolean

    suspend fun findOne(criteria: Criteria): T?

    fun findAll(
        criteria: Criteria? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun update(criteria: Criteria, patch: Patch<T>): T?

    suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T?

    fun updateAll(criteria: Criteria, patch: Patch<T>, limit: Int? = null, offset: Long? = null, sort: Sort? = null): Flow<T>

    fun updateAll(criteria: Criteria, patch: SuspendPatch<T>, limit: Int? = null, offset: Long? = null, sort: Sort? = null): Flow<T>

    suspend fun count(criteria: Criteria? = null, limit: Int? = null): Long

    suspend fun deleteAll(criteria: Criteria? = null, limit: Int? = null, offset: Long? = null, sort: Sort? = null)

    companion object
}

suspend fun <T : Any, ID : Any> QueryRepository<T, ID>.findOneOrFail(criteria: Criteria): T {
    return findOne(criteria) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> QueryRepository<T, ID>.updateOrFail(
    criteria: Criteria,
    patch: SuspendPatch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> QueryRepository<T, ID>.updateOrFail(
    criteria: Criteria,
    patch: Patch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> QueryRepository<T, ID>.updateOrFail(
    criteria: Criteria,
    patch: (entity: T) -> Unit
): T {
    return updateOrFail(criteria, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> QueryRepository<T, ID>.update(
    criteria: Criteria,
    patch: (entity: T) -> Unit
): T? {
    return update(criteria, Patch.with(patch))
}
