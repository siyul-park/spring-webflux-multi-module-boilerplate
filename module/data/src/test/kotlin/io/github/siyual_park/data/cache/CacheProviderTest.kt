package io.github.siyual_park.data.cache

import com.github.javafaker.Faker
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.util.username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration

class CacheProviderTest : CoroutineTestHelper() {
    private val faker = Faker()

    private val cacheBuilder = {
        CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofSeconds(30))
            .maximumSize(1_000)
    }

    @Test
    fun get() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = faker.name().username(10)
        val value1 = faker.name().username(10)
        val value2 = faker.name().username(10)
        assertEquals(value1, provider.get(key) { value1 })
        assertEquals(value1, provider.get(key) { value2 })
    }

    @Test
    fun getIfPresent() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = faker.name().username(10)
        val value1 = faker.name().username(10)
        val value2 = faker.name().username(10)

        assertNull(provider.getIfPresent(key) { null })
        assertEquals(value1, provider.getIfPresent(key) { value1 })
        assertEquals(value1, provider.getIfPresent(key) { value2 })
        assertEquals(value1, provider.getIfPresent(key))
    }

    @Test
    fun put() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = faker.name().username(10)
        val value1 = faker.name().username(10)
        val value2 = faker.name().username(10)

        provider.put(key, value1)
        assertEquals(value1, provider.getIfPresent(key))
        provider.put(key, value2)
        assertEquals(value2, provider.getIfPresent(key))
    }

    @Test
    fun remove() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = faker.name().username(10)
        val value = faker.name().username(10)

        provider.put(key, value)
        assertEquals(value, provider.getIfPresent(key))
        provider.remove(key)
        assertNull(provider.getIfPresent(key))
    }

    @Test
    fun entries() = blocking {
        val provider = CacheProvider<String, String>(cacheBuilder())

        val key = faker.name().username(10)
        val value = faker.name().username(10)

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

        val key = faker.name().username(10)
        val value = faker.name().username(10)

        provider.put(key, value)
        provider.clear()

        assertEquals(emptySet<Pair<String, String>>(), provider.entries())
        assertNull(provider.getIfPresent(key))
    }
}
