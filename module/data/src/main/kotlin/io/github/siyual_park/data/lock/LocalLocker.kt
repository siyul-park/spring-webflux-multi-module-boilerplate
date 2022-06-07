package io.github.siyual_park.data.lock

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.apache.commons.collections4.map.ReferenceMap
import java.util.Collections

class LocalLocker : Locker {
    private val mutex = Mutex()
    private val locks = Collections.synchronizedMap(
        ReferenceMap<String, LocalLock>(
            AbstractReferenceMap.ReferenceStrength.HARD,
            AbstractReferenceMap.ReferenceStrength.WEAK
        )
    )

    override suspend fun get(key: String): Lock {
        return mutex.withLock {
            locks.getOrPut(key) { LocalLock() }
        }
    }

    override suspend fun remove(key: String): Boolean {
        return mutex.withLock {
            locks.remove(key) != null
        }
    }
}
