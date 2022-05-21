package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.WeakHashMap

class AsyncPool<T : Any>(
    private val load: suspend () -> T
) {
    private val store = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
    private val mutex = Mutex()

    suspend fun poll(): T {
        return mutex.withLock {
            store.singleOrNull()?.also { store.remove(it) }
        } ?: load()
    }

    suspend fun add(value: T) {
        mutex.withLock {
            store.add(value)
        }
    }
}
