package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class SoftDeletedR2DBCRepository<T : SoftDeletable, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    clazz: KClass<T>,
    scheduler: Scheduler = Schedulers.boundedElastic(),
    eventPublisher: EventPublisher? = null,
) : R2DBCRepository<T, ID> {
    private val delegator = FilteredR2DBCRepository<T, ID>(
        entityOperations,
        clazz,
        scheduler,
        where(SoftDeletable::deletedAt).isNull,
        eventPublisher
    )

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
        return delegator.existsById(id)
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return delegator.exists(criteria)
    }

    override suspend fun findById(id: ID): T? {
        return delegator.findById(id)
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return delegator.findAllById(ids)
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return delegator.findOne(criteria)
    }

    override fun findAll(): Flow<T> {
        return delegator.findAll()
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return delegator.findAll(criteria, limit, offset, sort)
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return delegator.updateById(id, patch)
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return delegator.updateById(id, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return delegator.updateAllById(ids, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return delegator.updateAllById(ids, patch)
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return delegator.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return delegator.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return delegator.updateAll(entity, patch)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return delegator.update(entity, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return delegator.update(criteria, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        return delegator.update(criteria, patch)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T> {
        return delegator.updateAll(criteria, patch)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T> {
        return delegator.updateAll(criteria, patch)
    }

    override suspend fun update(entity: T): T? {
        return delegator.update(entity)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return delegator.update(entity, patch)
    }

    override suspend fun count(): Long {
        return delegator.count()
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(criteria)
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
        delegator.updateAll(
            delegator.findAll(criteria, limit, offset, sort).toList(),
            Patch.with {
                it.deletedAt = Instant.now()
            }
        ).collect { }
    }
}
