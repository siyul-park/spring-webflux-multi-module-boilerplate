package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.data.cache.CacheProvider
import io.github.siyual_park.test.DummyStringFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class CacheProviderTest : CoroutineTestHelper() {
    private val cacheBuilder = {
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofSeconds(30))
            .maximumSize(1_000)
    }

    @Test
    fun get() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value1 = DummyStringFactory.create(10)
        val value2 = DummyStringFactory.create(10)
        assertEquals(value1, provider.get(key) { value1 })
        assertEquals(value1, provider.get(key) { value2 })
    }

    @Test
    fun getIfPresent() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value1 = DummyStringFactory.create(10)
        val value2 = DummyStringFactory.create(10)

        assertNull(provider.getIfPresent(key) { null })
        assertEquals(value1, provider.getIfPresent(key) { value1 })
        assertEquals(value1, provider.getIfPresent(key) { value2 })
        assertEquals(value1, provider.getIfPresent(key))
    }

    @Test
    fun put() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value1 = DummyStringFactory.create(10)
        val value2 = DummyStringFactory.create(10)

        provider.put(key, value1)
        assertEquals(value1, provider.getIfPresent(key))
        provider.put(key, value2)
        assertEquals(value2, provider.getIfPresent(key))
    }

    @Test
    fun remove() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        provider.put(key, value)
        assertEquals(value, provider.getIfPresent(key))
        provider.remove(key)
        assertNull(provider.getIfPresent(key))
    }

    @Test
    fun entries() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        provider.put(key, value)
        assertEquals(setOf(key to value), provider.entries())
        provider.remove(key)
        assertEquals(emptySet<Pair<String, String>>(), provider.entries())
        provider.put(key, value)
        assertEquals(setOf(key to value), provider.entries())
    }

    @Test
    fun clear() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        provider.put(key, value)
        provider.clear()

        assertEquals(emptySet<Pair<String, String>>(), provider.entries())
        assertNull(provider.getIfPresent(key))
    }
}
