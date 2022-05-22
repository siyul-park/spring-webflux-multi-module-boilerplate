package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder

class InMemoryQueryStorageTest : QueryStorageTestHelper(InMemoryQueryStorage { CacheBuilder.newBuilder() })
