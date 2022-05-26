package io.github.siyual_park.data.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.ulid.ULID

class InMemoryStorageTest : StorageTestHelper(
    run {
        val idProperty = object : WeekProperty<Person, ULID?> {
            override fun get(entity: Person): ULID {
                return entity.id
            }
        }
        InMemoryStorage(
            { CacheBuilder.newBuilder() },
            idProperty
        )
    }
)
