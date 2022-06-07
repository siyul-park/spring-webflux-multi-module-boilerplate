package io.github.siyual_park.data.lock

interface Lock {
    suspend fun acquire()
    suspend fun tryAcquire(): Boolean
    suspend fun release()
}

suspend inline fun <T> Lock.with(action: () -> T): T {
    acquire()
    try {
        return action()
    } finally {
        release()
    }
}
