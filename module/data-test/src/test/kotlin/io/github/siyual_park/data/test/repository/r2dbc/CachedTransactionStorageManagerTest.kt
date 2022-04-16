package io.github.siyual_park.data.test.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.r2dbc.CacheTransactionSynchronization
import io.github.siyual_park.data.repository.r2dbc.R2DBCStorageManager
import io.github.siyual_park.data.test.R2DBCTest
import io.github.siyual_park.data.test.entity.Person
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.reactor.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.transaction.reactive.TransactionContextManager

@Suppress("UNCHECKED_CAST")
class CachedTransactionStorageManagerTest : R2DBCTest() {
    private val storage = InMemoryNestedStorage(
        CacheBuilder.newBuilder() as CacheBuilder<ULID, Person>,
        object : Extractor<Person, ULID> {
            override fun getKey(entity: Person): ULID {
                return entity.id
            }
        }
    )
    private val cacheTransactionSynchronization = CacheTransactionSynchronization<Person, ULID>()
    private val manager = R2DBCStorageManager(storage, cacheTransactionSynchronization)

    @Test
    fun getCurrent() = transactional {
        val current = manager.getCurrent()
        val transaction = TransactionContextManager.currentContext().awaitSingle()

        assertTrue(current != storage)
        assertEquals(storage, current.parent)
        assertEquals(current, cacheTransactionSynchronization.get(transaction))
    }
}
