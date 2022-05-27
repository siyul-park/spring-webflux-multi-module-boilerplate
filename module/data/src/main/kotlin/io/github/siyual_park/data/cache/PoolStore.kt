package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength

class PoolStore<T : Any>(
    type: ReferenceStrength = ReferenceStrength.SOFT
) {
    private val store = ReferenceStore<T>(type)
    private val mutex = Mutex()

    suspend fun push(value: T): Boolean {
        mutex.withLock {
            return store.push(value)
        }
    }

    suspend fun pop(): T? {
        return mutex.withLock {
            store.firstOrNull()?.also { store.remove(it) }
        }
    }

    suspend fun remove(value: T): Boolean {
        mutex.withLock {
            return store.remove(value)
        }
    }

    suspend fun forEach(action: suspend (T) -> Unit) {
        return mutex.withLock {
            store.entries().forEach { action(it) }
        }
    }
}
