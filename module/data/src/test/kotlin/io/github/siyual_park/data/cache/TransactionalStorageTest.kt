package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.ulid.ULID

class TransactionalStorageTest : StorageTestHelper(
    run {
        val idExtractor = object : Extractor<Person, ULID> {
            override fun getKey(entity: Person): ULID {
                return entity.id
            }
        }
        TransactionalStorage(
            PoolingNestedStorage(
                LoadingPool {
                    InMemoryStorage(
                        { CacheBuilder.newBuilder() },
                        idExtractor
                    )
                },
                idExtractor
            )
        )
    }
)
