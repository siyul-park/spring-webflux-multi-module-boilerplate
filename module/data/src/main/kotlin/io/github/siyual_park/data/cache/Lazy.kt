package io.github.siyual_park.data.cache

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Lazy<T : Any>(
    private val loader: () -> T
) {
    private var value: T? = null
    private val lock = ReentrantReadWriteLock()

    fun get(): T {
        return lock.read {
            this.value ?: lock.write {
                loader().also { this.value = it }
            }
        }
    }

    fun clear() {
        lock.write {
            value = null
        }
    }
}
