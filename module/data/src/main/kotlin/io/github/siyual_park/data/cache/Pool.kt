package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections

class Pool<T : Any>(
    type: ReferenceStrength = ReferenceStrength.SOFT
) {
    private val store = Collections.newSetFromMap(
        Collections.synchronizedMap(ReferenceMap<T, Boolean>(type, type))
    )
    private val mutex = Mutex()

    suspend fun add(value: T) {
        mutex.withLock {
            store.add(value)
        }
    }

    suspend fun remove(value: T) {
        mutex.withLock {
            store.remove(value)
        }
    }

    suspend fun poll(): T? {
        return mutex.withLock {
            store.singleOrNull()?.also { store.remove(it) }
        }
    }

    suspend fun forEach(action: suspend (T) -> Unit) {
        return mutex.withLock {
            store.forEach { action(it) }
        }
    }
}
