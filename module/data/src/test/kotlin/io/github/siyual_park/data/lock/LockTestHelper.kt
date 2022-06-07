package io.github.siyual_park.data.lock

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class LockTestHelper(
    private val lock: Lock
) : CoroutineTestHelper() {

    @Test
    fun tryAcquire() = blocking {
        assertTrue(lock.tryAcquire())
        assertFalse(lock.tryAcquire())
        lock.release()
        assertTrue(lock.tryAcquire())
    }

    @Test
    fun acquire() = blocking {
        lock.acquire()
        assertFalse(lock.tryAcquire())
        lock.release()
    }

    @Test
    fun release() = blocking {
        assertTrue(lock.tryAcquire())
        lock.release()
        assertTrue(lock.tryAcquire())
    }
}
