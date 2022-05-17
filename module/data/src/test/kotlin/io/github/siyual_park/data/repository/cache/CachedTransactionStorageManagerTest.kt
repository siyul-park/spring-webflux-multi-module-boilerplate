package io.github.siyual_park.data.repository.cache

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.Extractor
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.reactor.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.transaction.reactive.TransactionContextManager

@Suppress("UNCHECKED_CAST")
class CachedTransactionStorageManagerTest : DataTestHelper() {
    private val storage = InMemoryNestedStorage(
        InMemoryStorage(
            CacheBuilder.newBuilder() as CacheBuilder<ULID, Person>,
            object : Extractor<Person, ULID> {
                override fun getKey(entity: Person): ULID {
                    return entity.id
                }
            }
        )
    )
    private val cacheTransactionSynchronization = CacheTransactionSynchronization<Person, ULID>()
    private val manager = TransactionalStorageManager(storage, cacheTransactionSynchronization)

    @Test
    fun getCurrent() = transactional {
        val current = manager.getCurrent()
        val transaction = TransactionContextManager.currentContext().awaitSingle()

        assertTrue(current != storage)
        assertEquals(storage, current.parent)
        assertEquals(current, cacheTransactionSynchronization.get(transaction))
    }
}
