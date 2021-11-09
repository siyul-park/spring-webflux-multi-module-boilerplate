package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.repository.cache.CacheLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

class R2DBCCacheLoaderAdapter<T : Cloneable<T>, Key : Any>(
    private val repository: R2DBCRepository<T, Key>,
) : CacheLoader<T, Key>() {
    private val entityManager = repository.entityManager

    override suspend fun loadByKey(key: Key): T? {
        return repository.findById(key)
    }

    override fun loadByKeys(keys: Iterable<Key>): Flow<T?> {
        return flow {
            val result = mutableMapOf<Key, T>()
            repository.findAllById(keys).toList()
                .forEach {
                    val id = entityManager.getId(it)
                    result[id] = it
                }

            keys.forEach { emit(result[it]) }
        }
    }
}
