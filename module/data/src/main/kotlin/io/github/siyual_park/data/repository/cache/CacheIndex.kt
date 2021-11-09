package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections

class CacheIndex<T : Any, Key : Any>(
    val values: MutableMap<Key, T> = Collections.synchronizedMap(ReferenceMap()),
    val loader: CacheLoader<T, Key>
) {
    suspend fun findByKey(key: Key, cache: Boolean = true): T? {
        if (!cache) {
            return loader.loadByKey(key)
                ?.also { values[key] = it }
        }

        val value = values[key]
        return if (value == null) {
            val found = loader.loadByKey(key)
            if (found != null) {
                values[key] = found
                found
            } else {
                null
            }
        } else {
            value
        }
    }

    fun findAllByKey(keys: Iterable<Key>, cache: Boolean = true): Flow<T> {
        if (!cache) {
            return flow {
                val indexKeys = keys.toList()
                val result = loader.loadByKeys(keys).toList()

                result.forEachIndexed { index, entity ->
                    if (entity != null) {
                        values[indexKeys[index]] = entity
                    }
                }

                emitAll(result.filterNotNull().asFlow())
            }
        }

        return flow {
            val result = mutableMapOf<Int, T>()

            val notCachedKey = mutableListOf<Pair<Int, Key>>()
            keys.forEachIndexed { index, key ->
                val cached = values.getOrDefault(key, null)
                if (cached == null) {
                    notCachedKey.add(index to key)
                } else {
                    result[index] = cached
                }
            }

            loader.loadByKeys(notCachedKey.map { it.second }).toList()
                .forEachIndexed { index, entity ->
                    val (originIndex, key) = notCachedKey[index]

                    if (entity != null) {
                        values[key] = entity
                        result[originIndex] = entity
                    }
                }

            emitAll(result.values.asFlow())
        }
    }
}
