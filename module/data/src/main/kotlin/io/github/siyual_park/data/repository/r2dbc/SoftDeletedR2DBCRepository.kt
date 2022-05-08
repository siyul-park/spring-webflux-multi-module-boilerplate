package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class SoftDeletedR2DBCRepository<T : SoftDeletable, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    clazz: KClass<T>,
    private val eventPublisher: EventPublisher? = null,
) : R2DBCRepository<T, ID> {
    private val filteredRepository = FilteredR2DBCRepository<T, ID>(
        entityOperations,
        clazz,
        { where(SoftDeletable::deletedAt).isNull },
        eventPublisher
    )

    private val simpleRepository = SimpleR2DBCRepository<T, ID>(
        entityOperations,
        clazz,
    )

    override val entityManager: EntityManager<T, ID>
        get() = filteredRepository.entityManager

    override suspend fun create(entity: T): T {
        return filteredRepository.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return filteredRepository.createAll(entities)
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return filteredRepository.createAll(entities)
    }

    override suspend fun existsById(id: ID): Boolean {
        return filteredRepository.existsById(id)
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return filteredRepository.exists(criteria)
    }

    override suspend fun findById(id: ID): T? {
        return filteredRepository.findById(id)
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return filteredRepository.findAllById(ids)
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return filteredRepository.findOne(criteria)
    }

    override fun findAll(): Flow<T> {
        return filteredRepository.findAll()
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return filteredRepository.findAll(criteria, limit, offset, sort)
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return filteredRepository.updateById(id, patch)
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return filteredRepository.updateById(id, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return filteredRepository.updateAllById(ids, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return filteredRepository.updateAllById(ids, patch)
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return filteredRepository.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return filteredRepository.updateAll(entity)
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return filteredRepository.updateAll(entity, patch)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return filteredRepository.update(entity, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return filteredRepository.update(criteria, patch)
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        return filteredRepository.update(criteria, patch)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return filteredRepository.updateAll(criteria, patch, limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return filteredRepository.updateAll(criteria, patch, limit, offset, sort)
    }

    override suspend fun update(entity: T): T? {
        return filteredRepository.update(entity)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        return filteredRepository.update(entity, patch)
    }

    override suspend fun count(): Long {
        return filteredRepository.count()
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return filteredRepository.count(criteria)
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
        val target = simpleRepository.findAll(criteria, limit, offset, sort).toList()
        target.forEach { eventPublisher?.publish(BeforeDeleteEvent(it)) }
        simpleRepository.updateAll(
            target,
            Patch.with {
                it.deletedAt = Instant.now()
            }
        )
            .filterNotNull()
            .collect {
                eventPublisher?.publish(AfterDeleteEvent(it))
            }
    }
}
