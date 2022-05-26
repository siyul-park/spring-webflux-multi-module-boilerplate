package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person

class PoolingNestedQueryStorageTest : NestedQueryStorageTestHelper(
    PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(Person::class) { CacheBuilder.newBuilder() } })
)
