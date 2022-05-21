package io.github.siyual_park.data.cache

class LoadingPool<T : Any>(
    private val load: suspend () -> T
) {
    private val pool = Pool<T>()

    suspend fun poll(): T {
        return pool.poll() ?: load()
    }

    suspend fun add(value: T) {
        pool.add(value)
    }
}
