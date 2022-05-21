package io.github.siyual_park.data.cache

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength

class LoadingPool<T : Any>(
    private val load: suspend () -> T,
    type: ReferenceStrength = ReferenceStrength.SOFT
) {
    private val pool = Pool<T>(type)

    suspend fun poll(): T {
        return pool.poll() ?: load()
    }

    suspend fun add(value: T) {
        pool.add(value)
    }
}
