package io.github.siyual_park.data.repository.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class CacheLoader<T : Any, Key : Any> {

    open fun loadByKeys(keys: Iterable<Key>): Flow<T?> {
        return flow { keys.forEach { emit(loadByKey(it)) } }
    }

    abstract suspend fun loadByKey(key: Key): T?
}
