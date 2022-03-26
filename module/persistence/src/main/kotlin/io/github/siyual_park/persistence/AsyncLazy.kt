package io.github.siyual_park.persistence

import kotlinx.coroutines.sync.Semaphore

class AsyncLazy<T : Any>(
    private val loader: suspend () -> T
) {
    private var value: T? = null
    private val semaphore = Semaphore(1)

    suspend fun get(): T {
        return this.value
            ?: try {
                semaphore.acquire()
                this.value ?: loader().also { this.value = it }
            } finally {
                semaphore.release()
            }
    }

    fun clear() {
        value = null
    }
}
