package io.github.siyual_park.persistence

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AsyncLazy<T : Any>(
    private val loader: suspend () -> T
) {
    private var value: T? = null
    private val mutex = Mutex()

    suspend fun get(): T {
        return this.value
            ?: mutex.withLock { this.value ?: loader().also { this.value = it } }
    }

    suspend fun clear() {
        mutex.withLock {
            value = null
        }
    }
}
