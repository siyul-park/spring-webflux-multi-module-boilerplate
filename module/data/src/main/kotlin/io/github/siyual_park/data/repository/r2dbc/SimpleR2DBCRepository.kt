package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.data.event.AfterCreateEvent
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.data.event.BeforeCreateEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.r2dbc.core.Parameter
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KProperty1

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class SimpleR2DBCRepository<T : Any, ID : Any>(
    private val entityManager: EntityManager<T, ID?>,
    private val eventPublisher: EventPublisher? = null,
) : R2DBCRepository<T, ID> {
    private val entityOperations = entityManager.getOperations()
    private val clazz = entityManager.getClass()

    private val generatedValueColumn = run {
        val requiredEntity = entityManager.getRequiredEntity()

        val annotatedValueSqlIdentifier = mutableListOf<SqlIdentifier>()
        requiredEntity.forEach {
            if (it.isAnnotationPresent(GeneratedValue::class.java)) {
                annotatedValueSqlIdentifier.add(it.columnName)
            }
        }

        annotatedValueSqlIdentifier.toSet()
    }

    override suspend fun create(entity: T): T {
        eventPublisher?.publish(BeforeCreateEvent(entity))

        val saved = entityOperations.insert(entity)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()

        return entityOperations.select(
            query(where(entityManager.getIdColumnName()).`is`(saved))
                .limit(1),
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
            .also { eventPublisher?.publish(AfterCreateEvent(it)) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            val saved = entities.onEach { eventPublisher?.publish(BeforeCreateEvent(it)) }
                .map {
                    entityOperations.insert(it)
                        .subscribeOn(Schedulers.parallel())
                        .awaitSingle()
                }.toList()

            if (saved.isNotEmpty()) {
                emitAll(
                    entityOperations.select(
                        query(where(entityManager.getIdColumnName()).`in`(saved.map { entityManager.getId(it) }))
                            .limit(saved.size),
                        clazz.java
                    )
                        .subscribeOn(Schedulers.parallel())
                        .sort { p1, p2 ->
                            saved.indexOf(p1) - saved.indexOf(p2)
                        }
                        .asFlow()
                        .onEach { eventPublisher?.publish(AfterCreateEvent(it)) }
                )
            }
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return createAll(entities.asFlow())
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists(where(entityManager.getIdColumnName()).`is`(id))
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return entityOperations.exists(
            query(criteria),
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
    }

    override suspend fun findById(id: ID): T? {
        return findOne(where(entityManager.getIdColumnName()).`is`(id))
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return entityOperations.select(
            query(criteria)
                .limit(1),
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .awaitFirstOrNull()
    }

    override fun findAll(): Flow<T> {
        return findAll(criteria = null)
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }

        var query = query(criteria ?: CriteriaDefinition.empty())
        limit?.let {
            query = query.limit(it)
        }
        offset?.let {
            query = query.offset(it)
        }
        sort?.let {
            query = query.sort(sort)
        }

        return this.entityOperations.select(
            query,
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .asFlow()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        if (ids.count() == 0) {
            return emptyFlow()
        }

        return findAll(
            where(entityManager.getIdColumnName()).`in`(ids.toList()),
            limit = ids.count()
        )
            .asFlux()
            .sort { p1, p2 ->
                val p1Id = entityManager.getId(p1)
                val p2Id = entityManager.getId(p2)

                ids.indexOf(p1Id) - ids.indexOf(p2Id)
            }
            .asFlow()
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T): T? {
        return entityManager.getId(entity)?.let { updateById(it, SuspendPatch.from { entity }) }
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: SuspendPatch<T>): T? {
        return findOne(criteria)?.let { update(it, patch) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: SuspendPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return findAll(criteria, limit, offset, sort)
            .map { update(it, patch) }
            .filterNotNull()
    }

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        val originOutboundRow = entityManager.getOutboundRow(entity)

        val patched = patch.apply(entity)
        val patchedOutboundRow = entityManager.getOutboundRow(patched)

        val diff = diff(originOutboundRow, patchedOutboundRow)
        if (diff.isEmpty()) {
            return null
        }

        val propertyDiff = toProperty(diff)
        eventPublisher?.publish(BeforeUpdateEvent(entity, propertyDiff))

        val id = entityManager.getId(entity) ?: return null
        val updateCount = entityOperations.update(
            query(where(entityManager.getIdColumnName()).`is`(id))
                .limit(1),
            Update.from(diff as Map<SqlIdentifier, Any>),
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
        if (updateCount == 0) {
            return null
        }

        return findById(id)
            ?.also { eventPublisher?.publish(AfterUpdateEvent(it, propertyDiff)) }
    }

    override suspend fun count(): Long {
        return count(criteria = null)
    }

    override suspend fun count(criteria: CriteriaDefinition?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }

        return entityOperations.count(query(criteria ?: CriteriaDefinition.empty()), clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
    }

    override suspend fun deleteById(id: ID) {
        deleteAll(where(entityManager.getIdColumnName()).`is`(id))
    }

    override suspend fun delete(entity: T) {
        eventPublisher?.publish(BeforeDeleteEvent(entity))

        val id = entityManager.getId(entity) ?: return
        entityOperations.delete(query(where(entityManager.getIdColumnName()).`is`(id)), clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()

        eventPublisher?.publish(AfterDeleteEvent(entity))
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        if (ids.count() == 0) {
            return
        }

        deleteAll(where(entityManager.getIdColumnName()).`in`(ids.toList()))
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.forEach { eventPublisher?.publish(BeforeDeleteEvent(it)) }

        val ids = entities.mapNotNull { entityManager.getId(it) }
        if (ids.isEmpty()) {
            return
        }

        entityOperations.delete(
            query(where(entityManager.getIdColumnName()).`in`(ids.toList()))
                .limit(ids.size),
            clazz.java
        )
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()

        entities.forEach {
            eventPublisher?.publish(AfterDeleteEvent(it))
        }
    }

    override suspend fun deleteAll() {
        deleteAll(criteria = null)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }

        var query = query(criteria ?: CriteriaDefinition.empty())
        limit?.let {
            query = query.limit(it)
        }
        offset?.let {
            query = query.offset(it)
        }
        sort?.let {
            query = query.sort(sort)
        }

        if (eventPublisher == null) {
            entityOperations.delete(query, clazz.java)
                .subscribeOn(Schedulers.parallel())
                .awaitSingle()
        } else {
            entityOperations.select(
                query,
                clazz.java
            )
                .subscribeOn(Schedulers.parallel())
                .asFlow()
                .toList()
                .let { deleteAll(it) }
        }
    }

    private fun diff(source: OutboundRow, target: OutboundRow): Map<SqlIdentifier, Parameter> {
        val diff = mutableMapOf<SqlIdentifier, Parameter>()
        source.keys.forEach {
            val sourceValue = source[it]
            val targetValue = target[it]

            if (!generatedValueColumn.contains(it) && sourceValue.value != targetValue.value) {
                diff[it] = targetValue
            }
        }

        return diff
    }

    private fun toProperty(diff: Map<SqlIdentifier, Parameter>): Map<KProperty1<T, *>, Any?> {
        val propertyDiff = mutableMapOf<KProperty1<T, *>, Any?>()

        diff.forEach { (key, value) ->
            val property = entityManager.getProperty(key)
            if (property != null) {
                propertyDiff[property] = value.value
            }
        }

        return propertyDiff
    }

    companion object
}
