package io.github.siyual_park.data.cache

import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.test.DummyStringFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AsyncLazyTest : CoroutineTestHelper() {
    @Test
    fun get() = blocking {
        val asyncLazy = AsyncLazy { DummyStringFactory.create(10) }
        val value = asyncLazy.get()
        assertEquals(value, asyncLazy.get())
    }

    @Test
    fun pop() = blocking {
        val asyncLazy = AsyncLazy { DummyStringFactory.create(10) }
        assertNull(asyncLazy.pop())
        val value = asyncLazy.get()
        assertEquals(value, asyncLazy.pop())
        assertNull(asyncLazy.pop())
    }

    @Test
    fun clear() = blocking {
        val asyncLazy = AsyncLazy { DummyStringFactory.create(10) }
        asyncLazy.get()
        asyncLazy.clear()
        assertNull(asyncLazy.pop())
    }
}
