package io.github.siyual_park.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SuspendLazy<T : Any>(
    private val initializer: suspend () -> T
) {
    private var value: T? = null
    private var mutex: Mutex = Mutex()

    suspend fun get(): T {
        return this.value
            ?: mutex.withLock {
                this.value
                    ?: initializer().also {
                        this.value = it
                    }
            }
    }

    suspend fun pop(): T? {
        return mutex.withLock {
            val value = this.value ?: return@withLock null
            this.value = null
            value
        }
    }

    suspend fun clear() {
        mutex.withLock {
            value = null
        }
    }
}
