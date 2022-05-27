package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections

class PoolStore<T : Any>(
    type: ReferenceStrength = ReferenceStrength.SOFT
) {
    private val store = Collections.newSetFromMap(
        Collections.synchronizedMap(ReferenceMap<T, Boolean>(type, ReferenceStrength.HARD))
    )
    private val mutex = Mutex()

    suspend fun clear() {
        mutex.withLock {
            store.clear()
        }
    }

    suspend fun push(value: T): Boolean {
        mutex.withLock {
            return store.add(value)
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
            store.forEach { action(it) }
        }
    }

    fun entries(): Set<T> {
        return store.toSet()
    }
}
