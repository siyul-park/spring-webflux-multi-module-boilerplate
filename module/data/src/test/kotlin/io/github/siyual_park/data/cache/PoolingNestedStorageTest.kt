package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.Extractor
import io.github.siyual_park.ulid.ULID

class PoolingNestedStorageTest : NestedStorageTestHelper(
    run {
        val idExtractor = object : Extractor<Person, ULID> {
            override fun getKey(entity: Person): ULID {
                return entity.id
            }
        }
        PoolingNestedStorage(
            Pool {
                InMemoryStorage(
                    { CacheBuilder.newBuilder() },
                    idExtractor
                )
            },
            idExtractor
        )
    }
)
