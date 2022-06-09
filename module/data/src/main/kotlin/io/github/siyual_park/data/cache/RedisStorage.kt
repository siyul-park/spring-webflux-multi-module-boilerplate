package io.github.siyual_park.data.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.redisson.api.RedissonReactiveClient
import java.time.Duration
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class RedisStorage<ID : Any, T : Any>(
    redisClient: RedissonReactiveClient,
    name: String,
    private val ttl: Duration,
    private val size: Int,
    private val objectMapper: ObjectMapper,
    private val id: WeekProperty<T, ID?>,
    private val keyClass: KClass<ID>,
    private val valueClass: KClass<T>,
) : Storage<ID, T> {

    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()
    private val store = redisClient.getMapCache<String, String>(name)

    init {
        runBlocking {
            store.trySetMaxSize(size).awaitFirstOrNull()
            store.expire(ttl).awaitFirstOrNull()
        }
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
        return store.get(writeKey(index, key)).awaitFirstOrNull()?.let {
            getIfPresent(readId(it))
        }
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        return getIfPresent(index, key) ?: loader()?.also { add(it) }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return store.get(writeKey("_id", id)).awaitFirstOrNull()?.let {
            objectMapper.readValue(it, valueClass.java)
        }
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        return getIfPresent(id) ?: loader()?.also { add(it) }
    }

    override suspend fun remove(id: ID) {
        val entity = getIfPresent(id) ?: return

        val keys = mutableListOf<String>()
        keys.add(writeKey("_id", id))
        properties.forEach { (key, property) ->
            keys.add(writeKey(key, property.get(entity)))
        }

        store.fastRemove(*keys.toTypedArray()).awaitFirstOrNull()
    }

    override suspend fun add(entity: T) {
        val id = id.get(entity) ?: return

        val values = mutableMapOf<String, String>()
        values[writeKey("_id", id)] = writeValue(entity)
        properties.forEach { (key, property) ->
            values[writeKey(key, property.get(entity))] = writeValue(id)
        }

        store.putAll(values).awaitFirstOrNull()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return store.readAllMap().awaitSingle().entries
            .filter { (key) -> key.startsWith("_id:") }
            .map { (key, value) -> readId(key.removePrefix("_id:")) to readValue(value) }
            .toSet()
    }

    override suspend fun clear() {
        store.delete().awaitFirstOrNull()
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
}
