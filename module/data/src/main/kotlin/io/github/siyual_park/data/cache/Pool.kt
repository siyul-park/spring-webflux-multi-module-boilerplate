package io.github.siyual_park.data.cache

import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

class Pool<T : Any>(
    private val load: () -> T
) {
    private val store = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
    private val lock = ReentrantReadWriteLock()

    fun poll(): T {
        return lock.write { store.singleOrNull()?.also { store.remove(it) } } ?: load()
    }

    fun add(value: T) {
        lock.write { store.add(value) }
    }
}
