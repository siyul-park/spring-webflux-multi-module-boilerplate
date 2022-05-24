package io.github.siyual_park.data.repository.in_memory

import io.github.siyual_park.data.event.AfterCreateEvent
import io.github.siyual_park.data.event.AfterDeleteEvent
import io.github.siyual_park.data.event.AfterUpdateEvent
import io.github.siyual_park.data.event.BeforeCreateEvent
import io.github.siyual_park.data.event.BeforeDeleteEvent
import io.github.siyual_park.data.event.BeforeUpdateEvent
import io.github.siyual_park.data.event.CreateTimestamp
import io.github.siyual_park.data.event.UpdateTimestamp
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.event.EventBroadcaster
import io.github.siyual_park.event.EventEmitter
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.event.TypeMatchEventFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.annotation.Id
import java.util.Collections
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("UNCHECKED_CAST")
class SimpleInMemoryRepository<T : Any, ID : Any>(
    private val clazz: KClass<T>,
    eventPublisher: EventPublisher? = null,
) : InMemoryRepository<T, ID> {
    private val store = Collections.synchronizedMap(mutableMapOf<ID, T>())
    private val eventPublisher = EventBroadcaster()

    private val idProperty = (
        clazz.memberProperties.find { it.javaField?.annotations?.find { it is Id } != null }
            ?: throw RuntimeException()
        ) as KProperty1<T, ID?>

    init {
        val localEventEmitter = EventEmitter()
            .apply {
                on(TypeMatchEventFilter(BeforeCreateEvent::class), CreateTimestamp())
                on(TypeMatchEventFilter(BeforeUpdateEvent::class), UpdateTimestamp())
            }

        this.eventPublisher.use(localEventEmitter)
        eventPublisher?.let { this.eventPublisher.use(it) }
    }

    override suspend fun create(entity: T): T {
        eventPublisher.publish(BeforeCreateEvent(entity))

        val id = idProperty.get(entity)
        if (id?.let { existsById(it) } == true) {
            throw DataIntegrityViolationException("$id is already exists")
        }

        store[id] = entity
        eventPublisher.publish(AfterCreateEvent(entity))

        return entity
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return entities.map { create(it) }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return createAll(entities.asFlow())
    }

    override suspend fun existsById(id: ID): Boolean {
        return store[id] != null
    }

    override suspend fun findById(id: ID): T? {
        return store[id]
    }

    override fun findAll(): Flow<T> {
        return store.values.asFlow()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return ids.asFlow().map { findById(it) }.filterNotNull()
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return updateById(id, patch.async())
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
        return findById(id)?.let { update(it, patch) }
    }

    override suspend fun update(entity: T): T? {
        return idProperty.get(entity)
            ?.let { updateById(it, SuspendPatch.from { entity }) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        val sourceDump = mutableMapOf<KProperty1<T, *>, Any?>()
        clazz.memberProperties.forEach {
            sourceDump[it] = it.get(entity)
        }

        val target = patch.apply(entity)
        val propertyDiff = diff(sourceDump, target)

        val id = idProperty.get(entity)
        if (id?.let { !existsById(it) } == true) {
            return null
        }

        eventPublisher.publish(BeforeUpdateEvent(target, propertyDiff))

        propertyDiff.forEach { (property, value) ->
            if (property is KMutableProperty1<T, *>) {
                property as KMutableProperty1<T, Any?>
                property.set(target, value)
            }
        }

        store[idProperty.get(entity)] = entity

        eventPublisher.publish(AfterUpdateEvent(target, propertyDiff))

        return target
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        return ids.asFlow().map { updateById(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow().map { update(it) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        return entity.asFlow().map { update(it, patch) }
    }

    override suspend fun count(): Long {
        return store.size.toLong()
    }

    override suspend fun deleteById(id: ID) {
        findById(id)?.let { delete(it) }
    }

    override suspend fun delete(entity: T) {
        eventPublisher.publish(BeforeDeleteEvent(entity))

        val id = idProperty.get(entity) ?: return
        store.remove(id)

        eventPublisher.publish(AfterDeleteEvent(entity))
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        ids.forEach { deleteById(it) }
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        entities.forEach { delete(it) }
    }

    override suspend fun deleteAll() {
        deleteAll(store.values)
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
