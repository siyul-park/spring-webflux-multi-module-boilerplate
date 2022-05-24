package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

interface R2DBCRepository<T : Any, ID : Any> : Repository<T, ID> {
    suspend fun exists(criteria: CriteriaDefinition): Boolean

    suspend fun findOne(criteria: CriteriaDefinition): T?

    fun findAll(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T?

    suspend fun update(criteria: CriteriaDefinition, patch: SuspendPatch<T>): T?

    fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int? = null, offset: Long? = null, sort: Sort? = null): Flow<T>

    fun updateAll(criteria: CriteriaDefinition, patch: SuspendPatch<T>, limit: Int? = null, offset: Long? = null, sort: Sort? = null): Flow<T>

    suspend fun count(criteria: CriteriaDefinition? = null, limit: Int? = null): Long

    suspend fun deleteAll(criteria: CriteriaDefinition? = null, limit: Int? = null, offset: Long? = null, sort: Sort? = null)

    companion object
}

suspend fun <T : Any, ID : Any> R2DBCRepository<T, ID>.findOneOrFail(criteria: CriteriaDefinition): T {
    return findOne(criteria) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: SuspendPatch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: Patch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T {
    return updateOrFail(criteria, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> R2DBCRepository<T, ID>.update(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T? {
    return update(criteria, Patch.with(patch))
}
