package io.github.siyual_park.data.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.asDeferred
import org.redisson.api.RedissonClient
import java.lang.Long.max
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class RedisStorage<ID : Any, T : Any>(
    redisClient: RedissonClient,
    name: String,
    size: Int,
    private val objectMapper: ObjectMapper,
    private val id: WeekProperty<T, ID?>,
    private val expiredAt: WeekProperty<T, Instant?>,
    private val keyClass: KClass<ID>,
    private val valueClass: KClass<T>,
) : Storage<ID, T> {
    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()
    private val store = redisClient.getMapCache<String, String>(name)

    private val hit = AtomicLong()
    private val miss = AtomicLong()

    init {
        store.setMaxSize(size)
    }

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        properties[name] = property
    }

    override suspend fun removeIndex(name: String) {
        properties.remove(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return properties
    }

    override suspend fun containsIndex(name: String): Boolean {
        return properties.contains(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        return store.getAsync(writeKey(index, key)).asDeferred().await()?.let {
            getIfPresent(readId(it))
        }
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getIfPresent(index, key) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return store.getAsync(writeKey("_id", id)).asDeferred().await()?.let {
            objectMapper.readValue(it, valueClass.java)
        }.also { updateStatus(it) }
    }

    @Suppress("NAME_SHADOWING")
    override fun <KEY : Any> getAll(index: String, keys: Iterable<KEY>): Flow<T?> {
        return flow {
            val keys = keys.map { writeKey(index, it) }
            val ids = MutableList<ID?>(keys.size) { null }
            val result = MutableList<T?>(keys.size) { null }

            store.getAllAsync(keys.toSet()).asDeferred().await().forEach { (key, value) ->
                ids[keys.indexOf(key)] = readId(value)
            }

            val notNullIds = ids.filterNotNull()
            getAll(notNullIds).collectIndexed { index, value ->
                val id = notNullIds[index]
                val originIndex = ids.indexOf(id)
                result[originIndex] = value
            }

            emitAll(result.asFlow())
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun getAll(ids: Iterable<ID>): Flow<T?> {
        return flow {
            val ids = ids.map { writeKey("_id", it) }
            val result = MutableList<T?>(ids.size) { null }

            store.getAllAsync(ids.toSet()).asDeferred().await().forEach { (key, value) ->
                result[ids.indexOf(key)] = objectMapper.readValue(value, valueClass.java)
            }

            result.forEach { updateStatus(it) }

            emitAll(result.asFlow())
        }
    }

    override suspend fun remove(id: ID) {
        val entity = getIfPresent(id) ?: return

        val keys = mutableListOf<String>()
        keys.add(writeKey("_id", id))
        properties.forEach { (key, property) ->
            keys.add(writeKey(key, property.get(entity)))
        }

        store.fastRemoveAsync(*keys.toTypedArray()).asDeferred().await()
    }

    override suspend fun add(entity: T) {
        val id = id.get(entity) ?: return

        val values = mutableMapOf<String, String>()
        values[writeKey("_id", id)] = writeValue(entity)
        properties.forEach { (key, property) ->
            values[writeKey(key, property.get(entity))] = writeValue(id)
        }

        val expiredAt = expiredAt.get(entity)
        val now = Instant.now()
        store.putAllAsync(values, max(Duration.between(now, expiredAt).toSeconds(), 0L), TimeUnit.SECONDS).asDeferred().await()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return store.readAllMapAsync().asDeferred().await().entries
            .filter { (key) -> key.startsWith("_id:") }
            .map { (key, value) -> readId(key.removePrefix("_id:")) to readValue(value) }
            .toSet()
    }

    override suspend fun clear() {
        hit.set(0)
        miss.set(0)
        store.deleteAsync().asDeferred().await()
    }

    override suspend fun status(): Status {
        return Status(hit.get(), miss.get())
    }

    private fun <T> writeKey(type: String, value: T): String {
        return "$type:${writeValue(value)}"
    }

    private fun <T> writeValue(value: T): String {
        return objectMapper.writeValueAsString(value)
    }

    private fun readId(value: String): ID {
        return objectMapper.readValue(value, keyClass.java)
    }

    private fun readValue(value: String): T {
        return objectMapper.readValue(value, valueClass.java)
    }

    private fun updateStatus(value: T?) {
        if (value == null) {
            miss.incrementAndGet()
        } else {
            hit.incrementAndGet()
        }
    }
}
