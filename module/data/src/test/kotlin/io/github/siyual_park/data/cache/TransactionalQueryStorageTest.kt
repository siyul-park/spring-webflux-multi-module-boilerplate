package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class TransactionalQueryStorageTest : QueryStorageTestHelper(
    TransactionalQueryStorage(
        PoolingNestedQueryStorage(
            LoadingPool { InMemoryQueryStorage { CacheBuilder.newBuilder() } }
        )
    )
)
