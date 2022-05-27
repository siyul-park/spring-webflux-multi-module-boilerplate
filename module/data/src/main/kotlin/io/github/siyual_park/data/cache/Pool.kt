package io.github.siyual_park.data.cache

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength

class Pool<T : Any>(
    type: ReferenceStrength = ReferenceStrength.SOFT,
    private val load: suspend () -> T,
) {
    private val free = PoolStore<T>(type)
    private val used = PoolStore<T>(type)

    suspend fun pop(): T {
        val value = free.pop() ?: load()
        used.push(value)
        return value
    }

    suspend fun push(value: T): Boolean {
        used.remove(value)
        return free.push(value)
    }

    fun free(): PoolStore<T> {
        return free
    }

    fun used(): PoolStore<T> {
        return used
    }
}
