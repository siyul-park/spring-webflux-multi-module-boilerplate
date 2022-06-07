package io.github.siyual_park.data.lock

import kotlinx.coroutines.sync.Mutex

class LocalLock : Lock {
    private val mutex = Mutex()

    override suspend fun acquire() {
        mutex.lock()
    }

    override suspend fun tryAcquire(): Boolean {
        return mutex.tryLock()
    }

    override suspend fun release() {
        mutex.unlock()
    }
}
