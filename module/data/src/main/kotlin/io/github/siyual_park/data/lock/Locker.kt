package io.github.siyual_park.data.lock

interface Locker {
    suspend fun get(key: String): Lock
    suspend fun remove(key: String): Boolean
}

suspend inline fun <T> Locker.withLock(key: String, action: () -> T): T {
    val lock = get(key)
    try {
        return lock.with(action)
    } finally {
        remove(key)
    }
}
