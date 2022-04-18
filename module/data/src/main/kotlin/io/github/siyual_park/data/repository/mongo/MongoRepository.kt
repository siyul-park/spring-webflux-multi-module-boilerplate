package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import kotlin.reflect.KClass

interface MongoRepository<T : Any, ID : Any> : Repository<T, ID> {
    val template: ReactiveMongoTemplate
    val clazz: KClass<T>

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

    suspend fun deleteAll(criteria: CriteriaDefinition? = null, limit: Int? = null, offset: Long? = null, sort: Sort? = null)
}

suspend fun <T : Any, ID : Any> MongoRepository<T, ID>.findOneOrFail(criteria: CriteriaDefinition): T {
    return findOne(criteria) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> MongoRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: AsyncPatch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> MongoRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: Patch<T>
): T {
    return update(criteria, patch) ?: throw EmptyResultDataAccessException(1)
}

suspend fun <T : Any, ID : Any> MongoRepository<T, ID>.updateOrFail(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T {
    return updateOrFail(criteria, Patch.with(patch))
}

suspend fun <T : Any, ID : Any> MongoRepository<T, ID>.update(
    criteria: CriteriaDefinition,
    patch: (entity: T) -> Unit
): T? {
    return update(criteria, Patch.with(patch))
}
