package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asFlux
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class FilteredR2DBCRepository<T : Any, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    clazz: KClass<T>,
    scheduler: Scheduler = Schedulers.boundedElastic(),
    private val filter: () -> Criteria,
    eventPublisher: EventPublisher? = null,
) : R2DBCRepository<T, ID> {
    private val delegator = SimpleR2DBCRepository<T, ID>(entityOperations, clazz, scheduler, eventPublisher)

    override val entityManager: EntityManager<T, ID>
        get() = delegator.entityManager

    override suspend fun create(entity: T): T {
        return delegator.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return delegator.createAll(entities)
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return delegator.createAll(entities)
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists(where(entityManager.idProperty).`is`(id))
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return delegator.exists(filtered(criteria))
    }

    override suspend fun findById(id: ID): T? {
        return findOne(where(entityManager.idProperty).`is`(id))
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        if (ids.count() == 0) {
            return emptyFlow()
        }

        return findAll(where(entityManager.idProperty).`in`(ids.toList()))
            .asFlux()
            .sort { p1, p2 ->
                val p1Id = entityManager.getId(p1)
                val p2Id = entityManager.getId(p2)

                ids.indexOf(p1Id) - ids.indexOf(p2Id)
            }
            .asFlow()
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return delegator.findOne(filtered(criteria))
    }

    override fun findAll(): Flow<T> {
        return delegator.findAll(filtered(null))
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return delegator.findAll(filtered(criteria), limit, offset, sort)
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        return findOne(criteria)?.let { update(it, patch) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return findAll(criteria, limit, offset, sort)
            .map { update(it, patch) }
            .filterNotNull()
    }

    override suspend fun update(entity: T): T? {
        return delegator.update(entity)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return delegator.update(entity, patch)
    }

    override suspend fun count(): Long {
        return count(null)
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(filtered(criteria))
    }

    override suspend fun deleteById(id: ID) {
        deleteAll(where(entityManager.idProperty).`is`(id))
    }

    override suspend fun delete(entity: T) {
        val id = entityManager.getId(entity)
        deleteAll(where(entityManager.idProperty).`is`(id))
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        if (ids.count() == 0) {
            return
        }

        deleteAll(where(entityManager.idProperty).`in`(ids.toList()))
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        val ids = entities.map { entityManager.getId(it) }
        if (ids.count() == 0) {
            return
        }

        deleteAll(where(entityManager.idProperty).`in`(ids))
    }

    override suspend fun deleteAll() {
        return deleteAll(null)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        return delegator.deleteAll(criteria, limit, offset, sort)
    }

    private fun filtered(criteria: CriteriaDefinition?): CriteriaDefinition {
        return if (criteria == null) {
            filter()
        } else {
            filter().and(criteria)
        }
    }
}
