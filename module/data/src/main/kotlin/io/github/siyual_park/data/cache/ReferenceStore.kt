package io.github.siyual_park.data.cache

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections

class ReferenceStore<T : Any>(
    type: ReferenceStrength = ReferenceStrength.SOFT
) {
    private val store = Collections.newSetFromMap(
        Collections.synchronizedMap(ReferenceMap<T, Boolean>(type, ReferenceStrength.HARD))
    )

    fun clear() {
        store.clear()
    }

    fun firstOrNull(): T? {
        return store.firstOrNull()
    }

    fun push(value: T): Boolean {
        return store.add(value)
    }

    fun remove(value: T): Boolean {
        return store.remove(value)
    }

    fun entries(): Set<T> {
        return store.toSet()
    }
}
