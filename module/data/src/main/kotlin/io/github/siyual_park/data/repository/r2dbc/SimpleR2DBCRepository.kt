package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
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
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order.asc
import org.springframework.data.domain.Sort.by
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class SimpleR2DBCRepository<T : Any, ID : Any>(
    private val entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
    private val scheduler: Scheduler = Schedulers.boundedElastic(),
    private val eventPublisher: EventPublisher? = null,
) : R2DBCRepository<T, ID> {
    private val generatedValueColumn: Set<SqlIdentifier>

    override val entityManager = EntityManager<T, ID>(entityOperations, clazz)

    init {
        generatedValueColumn = getAnnotatedSqlIdentifier(GeneratedValue::class)
    }

    override suspend fun create(entity: T): T {
        val saved = this.entityOperations.insert(entity)
            .subscribeOn(scheduler)
            .awaitSingle()

        return this.entityOperations.select(
            query(where(entityManager.idProperty).`is`(saved)).limit(1),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
            .also { eventPublisher?.publish(AfterSaveEvent(it)) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        val saved = entities.map {
            this.entityOperations.insert(it)
                .subscribeOn(scheduler)
                .awaitSingle()
        }

        return flow {
            emitAll(
                entityOperations.select(
                    query(where(entityManager.idProperty).`in`(saved.toList())),
                    clazz.java
                )
                    .subscribeOn(scheduler)
                    .asFlow()
                    .onEach { eventPublisher?.publish(AfterSaveEvent(it)) }
            )
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return createAll(entities.asFlow())
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists(where(entityManager.idProperty).`is`(id))
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return this.entityOperations.exists(
            query(criteria),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override suspend fun findById(id: ID): T? {
        return findOne(where(entityManager.idProperty).`is`(id))
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return this.entityOperations.selectOne(
            query(criteria)
                .limit(1),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
    }

    override fun findAll(): Flow<T> {
        return findAll(criteria = null)
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        var query = query(criteria ?: CriteriaDefinition.empty())
        limit?.let {
            query = query.limit(it)
        }
        offset?.let {
            query = query.offset(it)
        }
        query = query.sort(sort ?: by(asc(entityManager.idProperty)))

        return this.entityOperations.select(
            query,
            clazz.java
        )
            .subscribeOn(scheduler)
            .asFlow()
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

    override suspend fun update(entity: T): T? {
        val originOutboundRow = entityManager.getOutboundRow(entity)

        val patch = mutableMapOf<SqlIdentifier, Any>()
        originOutboundRow.forEach { (key, value) ->
            if (!generatedValueColumn.contains(key)) {
                patch[key] = value
            }
        }

        val updateCount = this.entityOperations.update(
            query(where(entityManager.idProperty).`is`(entityManager.getId(entity))),
            Update.from(patch),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
        if (updateCount == 0) {
            return null
        }

        return findById(entityManager.getId(originOutboundRow))
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        return findOne(criteria)?.let { update(it, patch) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T> {
        return updateAll(criteria, patch.async())
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T> {
        return findAll(criteria)
            .map { update(it, patch) }
            .filterNotNull()
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        val originOutboundRow = entityManager.getOutboundRow(entity)

        val patched = patch.apply(entity)
        val patchedOutboundRow = entityManager.getOutboundRow(patched)

        val diff = mutableMapOf<SqlIdentifier, Any?>()
        originOutboundRow.keys.forEach {
            val originValue = originOutboundRow[it]
            val patchedValue = patchedOutboundRow[it]

            if (!generatedValueColumn.contains(it) && originValue.value != patchedValue.value) {
                diff[it] = patchedValue
            }
        }

        if (diff.isEmpty()) {
            return null
        }

        val updateCount = this.entityOperations.update(
            query(where(entityManager.idProperty).`is`(entityManager.getId(originOutboundRow))),
            Update.from(diff),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
        if (updateCount == 0) {
            return null
        }

        return findById(entityManager.getId(patchedOutboundRow))
            .also { eventPublisher?.publish(AfterSaveEvent(it)) }
    }

    override suspend fun count(): Long {
        return count(criteria = null)
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return this.entityOperations.count(query(criteria ?: CriteriaDefinition.empty()), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingle()
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
        deleteAll(criteria = null)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?) {
        this.entityOperations.delete(query(criteria ?: CriteriaDefinition.empty()), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    private fun <S : Annotation> getAnnotatedSqlIdentifier(annotationType: KClass<S>): Set<SqlIdentifier> {
        val requiredEntity = entityManager.getRequiredEntity(clazz.java)

        val annotatedValueSqlIdentifier = mutableListOf<SqlIdentifier>()
        requiredEntity.forEach {
            if (it.isAnnotationPresent(annotationType.java)) {
                annotatedValueSqlIdentifier.add(it.columnName)
            }
        }

        return annotatedValueSqlIdentifier.toSet()
    }
}
