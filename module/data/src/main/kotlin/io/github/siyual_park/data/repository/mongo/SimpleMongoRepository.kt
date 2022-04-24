package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.event.AfterCreateEvent
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.data.event.BeforeCreateEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.data.expansion.fieldName
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
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.where
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("UNCHECKED_CAST")
class SimpleMongoRepository<T : Any, ID : Any>(
    override val template: ReactiveMongoTemplate,
    override val clazz: KClass<T>,
    private val scheduler: Scheduler = Schedulers.boundedElastic(),
    private val eventPublisher: EventPublisher? = null,
) : MongoRepository<T, ID> {
    private val idProperty = (
        clazz.memberProperties.find { it.javaField?.annotations?.find { it is Id } != null }
            ?: throw RuntimeException()
        ) as KProperty1<T, ID>

    override suspend fun create(entity: T): T {
        eventPublisher?.publish(BeforeCreateEvent(entity))

        return template.insert(entity)
            .subscribeOn(scheduler)
            .awaitSingle()
            .also { eventPublisher?.publish(AfterCreateEvent(it)) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            emitAll(
                template.insertAll(
                    entities.onEach { eventPublisher?.publish(BeforeCreateEvent(it)) }
                        .toList()
                )
                    .subscribeOn(scheduler)
                    .asFlow()
                    .onEach { eventPublisher?.publish(AfterCreateEvent(it)) }
            )
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return createAll(entities.asFlow())
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists(where(idProperty).`is`(id))
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return template.exists(query(criteria), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override suspend fun findById(id: ID): T? {
        return template.findById(id, clazz.java)
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        return template.findOne(query(criteria), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
    }

    override fun findAll(): Flow<T> {
        return findAll(criteria = null)
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        var query = if (criteria == null) {
            Query()
        } else {
            query(criteria)
        }
        limit?.let {
            query = query.limit(it)
        }
        offset?.let {
            query = query.skip(it)
        }
        sort?.let {
            query = query.with(sort)
        }

        return template.find(
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

        return findAll(where(idProperty).`in`(ids.toList()), limit = ids.count())
            .asFlux()
            .sort { p1, p2 ->
                val p1Id = idProperty.get(p1)
                val p2Id = idProperty.get(p2)

                ids.indexOf(p1Id) - ids.indexOf(p2Id)
            }
            .asFlow()
    }

    override suspend fun update(criteria: CriteriaDefinition, update: Update): T? {
        return findOne(criteria)?.let { update(it, update) }
    }

    override suspend fun update(entity: T, update: Update): T? {
        val sourceDump = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            sourceDump[it] = it.get(entity)
        }

        return template.findAndModify(
            query(where(idProperty).`is`(idProperty.get(entity))).limit(1),
            update,
            FindAndModifyOptions().returnNew(true),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
            ?.also { target ->
                val propertyDiff = diff(sourceDump, target)
                eventPublisher?.publish(AfterUpdateEvent(target, propertyDiff))
            }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return updateById(id, patch.async())
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return update(where(idProperty).`is`(id), patch)
    }

    override suspend fun update(entity: T): T? {
        val source = findById(idProperty.get(entity)) ?: return null

        val propertyDiff = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            val sourceValue = it.get(source)
            val targetValue = it.get(entity)

            if (sourceValue != targetValue) {
                propertyDiff[it] = targetValue
            }
        }

        eventPublisher?.publish(BeforeUpdateEvent(entity, propertyDiff))

        return template.findAndModify(
            query(where(idProperty).`is`(idProperty.get(entity))).limit(1),
            Update().also {
                propertyDiff.forEach { (key, value) ->
                    it[fieldName(key)] = value
                }
            },
            FindAndModifyOptions().returnNew(true),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
            ?.also { eventPublisher?.publish(AfterUpdateEvent(it, propertyDiff)) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        val sourceDump = mutableMapOf<KProperty1<T, Any?>, Any?>()
        clazz.memberProperties.forEach {
            sourceDump[it] = it.get(entity)
        }

        val target = patch.apply(entity)
        val propertyDiff = diff(sourceDump, target)

        if (eventPublisher != null) {
            sourceDump.forEach { (key, value) ->
                if (key is KMutableProperty1<T, Any?>) {
                    key.set(target, value)
                }
            }
            eventPublisher.publish(BeforeUpdateEvent(target, propertyDiff))
        }

        return template.findAndModify(
            query(where(idProperty).`is`(idProperty.get(entity))).limit(1),
            Update().also {
                propertyDiff.forEach { (key, value) ->
                    it[fieldName(key)] = value
                }
            },
            FindAndModifyOptions().returnNew(true),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
            ?.also { eventPublisher?.publish(AfterUpdateEvent(it, propertyDiff)) }
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        return findOne(criteria)?.let { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
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
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return findAll(criteria, limit, offset, sort)
            .map { update(it, patch) }
            .filterNotNull()
    }

    override suspend fun count(): Long {
        return count(criteria = null)
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return template.count(
            if (criteria == null) {
                Query()
            } else {
                query(criteria)
            },
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override suspend fun deleteById(id: ID) {
        deleteAll(where(idProperty).`is`(id))
    }

    override suspend fun delete(entity: T) {
        eventPublisher?.publish(BeforeDeleteEvent(entity))

        template.findAndRemove(
            query(where(idProperty).`is`(idProperty.get(entity))).limit(1),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()

        eventPublisher?.publish(AfterDeleteEvent(entity))
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        if (ids.count() == 0) {
            return
        }

        deleteAll(where(idProperty).`in`(ids.toList()), limit = ids.count())
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        val ids = entities.map { idProperty.get(it) }
        return deleteAllById(ids)
    }

    override suspend fun deleteAll() {
        deleteAll(criteria = null)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        var query = if (criteria == null) {
            Query()
        } else {
            query(criteria)
        }
        limit?.let {
            query = query.limit(it)
        }
        offset?.let {
            query = query.skip(it)
        }
        query = query.with(sort ?: Sort.by(Sort.Order.asc(fieldName(idProperty))))

        if (eventPublisher == null) {
            template.findAllAndRemove(
                query,
                clazz.java
            )
                .subscribeOn(scheduler)
                .collect { }
        } else {
            val entities = template.find(query, clazz.java)
                .asFlow()
                .onEach { eventPublisher.publish(BeforeDeleteEvent(it)) }
                .toList()
            val ids = entities.map { idProperty.get(it) }
            if (ids.isEmpty()) {
                return
            }

            template.findAllAndRemove(
                query(where(idProperty).`in`(ids.toList())).limit(ids.size),
                clazz.java
            )
                .subscribeOn(scheduler)
                .collect { eventPublisher.publish(AfterDeleteEvent(it)) }
        }
    }

    private fun diff(source: Map<KProperty1<T, *>, Any?>, target: T): Map<KProperty1<T, *>, Any?> {
        val propertyDiff = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            val sourceValue = source[it]
            val targetValue = it.get(target)

            if (sourceValue != targetValue) {
                propertyDiff[it] = targetValue
            }
        }

        return propertyDiff
    }
}
