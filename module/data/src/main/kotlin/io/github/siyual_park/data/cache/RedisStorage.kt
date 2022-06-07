package io.github.siyual_park.data.cache

import com.google.common.collect.Maps
import io.github.siyual_park.data.WeekProperty
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.redisson.api.RMapCacheReactive
import org.redisson.api.RedissonReactiveClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Suppress("UNCHECKED_CAST")
class RedisStorage<ID : Any, T : Any>(
    private val redisClient: RedissonReactiveClient,
    private val configuration: Configuration,
    private val id: WeekProperty<T, ID?>
) : Storage<ID, T> {
    data class Configuration(
        val name: String,
        val ttl: Duration,
        val size: Int
    )

    private val name = configuration.name
    private val ttl = configuration.ttl

    private val indexes = Maps.newConcurrentMap<String, RMapCacheReactive<*, ID>>()
    private val properties = Maps.newConcurrentMap<String, WeekProperty<T, *>>()
    private val store = redisClient.getMapCache<ID, T>("cache:data:$name")

    init {
        runBlocking {
            store.setMaxSize(configuration.size).awaitFirstOrNull()
        }
    }

    override suspend fun <KEY : Any> createIndex(name: String, property: WeekProperty<T, KEY>) {
        indexes[name] = redisClient.getMapCache<KEY, ID>("cache:index:$name")
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
        TODO("Not yet implemented")
    }

    override suspend fun <KEY : Any> getIfPresent(index: String, key: KEY, loader: suspend () -> T?): T? {
        TODO("Not yet implemented")
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
            index.put(key, id, ttl.toSeconds(), TimeUnit.SECONDS).awaitFirstOrNull()
        }
        store.put(id, entity, ttl.toSeconds(), TimeUnit.SECONDS).awaitFirstOrNull()
    }

    override suspend fun entries(): Set<Pair<ID, T>> {
        return store.readAllMap().awaitSingle().entries.map { it.key to it.value }.toSet()
    }

    override suspend fun clear() {
        store.delete().awaitFirstOrNull()
        indexes.forEach { (_, index) -> index.delete().awaitFirstOrNull() }
    }
}
