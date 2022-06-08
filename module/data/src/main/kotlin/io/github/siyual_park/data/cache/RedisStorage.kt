package io.github.siyual_park.data.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.redisson.api.RMapCacheReactive
import org.redisson.api.RedissonReactiveClient
import org.redisson.codec.TypedJsonJacksonCodec
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class RedisStorage<ID : Any, T : Any>(
    private val redisClient: RedissonReactiveClient,
    private val name: String,
    private val ttl: Duration,
    private val size: Int,
    private val objectMapper: ObjectMapper,
    private val id: WeekProperty<T, ID?>,
    private val keyClass: KClass<ID>,
    valueClass: KClass<T>,
) : Storage<ID, T> {

    private val indexes = Maps.newConcurrentMap<String, RMapCacheReactive<*, ID>>()
    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()
    private val codec = TypedJsonJacksonCodec(keyClass.java, valueClass.java, objectMapper)
    private val store = redisClient.getMapCache<ID, T>(name, codec)

    init {
        runBlocking {
            store.trySetMaxSize(size).awaitFirstOrNull()
        }
    }

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        val codec = TypedJsonJacksonCodec(keyClass.java, objectMapper)
        indexes[name] = redisClient.getMapCache<KEY, ID>("${this.name}:$name", codec)
        properties[name] = property
    }

    override suspend fun removeIndex(name: String) {
        indexes.remove(name)
        properties.remove(name)
    }

    override suspend fun getIndexes(): Map<String, WeekProperty<T, *>> {
        return properties
    }

    override suspend fun containsIndex(name: String): Boolean {
        return properties.contains(name)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY): T? {
        val indexMap = indexes[index] ?: return null
        indexMap as RMapCacheReactive<Any, ID>
        val id = indexMap.get(key).awaitFirstOrNull() ?: return null

        return getIfPresent(id)
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        val indexMap = indexes[index] ?: return null
        indexMap as RMapCacheReactive<Any, ID>
        val id = indexMap.get(key).awaitFirstOrNull()

        return if (id == null) {
            val entity = loader()
            if (entity == null) {
                null
            } else {
                this.id.get(entity)?.let {
                    this.getIfPresent(it) { entity }
                }
            }
        } else {
            getIfPresent(id, loader)
        }
    }

    override suspend fun getIfPresent(id: ID): T? {
        return store.get(id).awaitFirstOrNull()
    }

    override suspend fun getIfPresent(id: ID, loader: suspend () -> T?): T? {
        val existed = store.get(id).awaitFirstOrNull()
        if (existed != null) {
            return existed
        }

        return loader()?.also { add(it) }
    }

    override suspend fun remove(id: ID) {
        val entity = getIfPresent(id) ?: return
        indexes.forEach { (name, index) ->
            val property = properties[name] ?: return@forEach
            val key = property.get(entity) ?: return@forEach
            index as RMapCacheReactive<Any, ID>
            index.remove(key, entity).awaitFirstOrNull()
        }
        store.remove(id, entity).awaitFirstOrNull()
    }

    override suspend fun add(entity: T) {
        val id = id.get(entity) ?: return

        indexes.forEach { (name, index) ->
            val property = properties[name] ?: return@forEach
            val key = property.get(entity) ?: return@forEach
            index as RMapCacheReactive<Any, ID>
            index.fastPut(key, id, ttl.toSeconds(), TimeUnit.SECONDS).awaitFirstOrNull()
        }
        store.fastPut(id, entity, ttl.toSeconds(), TimeUnit.SECONDS).awaitFirstOrNull()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return store.readAllMap().awaitSingle().entries.map { it.key to it.value }.toSet()
    }

    override suspend fun clear() {
        store.delete().awaitFirstOrNull()
        indexes.forEach { (_, index) -> index.delete().awaitFirstOrNull() }
    }
}
