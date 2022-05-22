package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class PoolingNestedQueryStorageTest : NestedQueryStorageTestHelper(
    PoolingNestedQueryStorage(Pool { InMemoryQueryStorage { CacheBuilder.newBuilder() } })
)
