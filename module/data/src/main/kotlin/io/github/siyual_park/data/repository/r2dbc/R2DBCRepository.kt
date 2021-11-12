package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.CriteriaDefinition

interface R2DBCRepository<T : Cloneable<T>, ID : Any> : Repository<T, ID> {
    val entityManager: EntityManager<T, ID>

    suspend fun exists(criteria: CriteriaDefinition): Boolean

    suspend fun findOne(criteria: CriteriaDefinition): T?

    fun findAll(
        criteria: CriteriaDefinition? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T?

    suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T?

    fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T>

    fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T>

    suspend fun count(criteria: CriteriaDefinition? = null): Long

    suspend fun deleteAll(criteria: CriteriaDefinition? = null)
}

suspend fun <T : Cloneable<T>, ID : Any> R2DBCRepository<T, ID>.findOneOrFail(criteria: CriteriaDefinition): T {
    return findOne(criteria) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Cloneable<T>, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: AsyncPatch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Cloneable<T>, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: Patch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Cloneable<T>, ID : Any> R2DBCRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T {
    return updateOrFail(criteria, Patch.with(patch))
}

suspend fun <T : Cloneable<T>, ID : Any> R2DBCRepository<T, ID>.update(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T? {
    return update(criteria, Patch.with(patch))
}
