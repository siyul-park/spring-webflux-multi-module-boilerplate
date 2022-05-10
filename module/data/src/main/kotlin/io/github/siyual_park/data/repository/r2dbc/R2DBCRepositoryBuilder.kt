package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.cache.TransactionalStorageManager
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import kotlin.reflect.KClass

class R2DBCRepositoryBuilder<T : Any, ID : Any>(
    private val entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
) {
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: CacheBuilder<Any, Any>? = null

    fun set(eventPublisher: EventPublisher?): R2DBCRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun set(cacheBuilder: CacheBuilder<Any, Any>?): R2DBCRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun build(): R2DBCRepository<T, ID> {
        val cacheBuilder = cacheBuilder

        val current = SimpleR2DBCRepository<T, ID>(
            entityOperations,
            clazz,
            eventPublisher
        )

        return if (cacheBuilder != null) {
            val idExtractor = createIdExtractor(current)

            CachedR2DBCRepository(
                current,
                TransactionalStorageManager(
                    InMemoryNestedStorage(
                        cacheBuilder as CacheBuilder<ID, T>,
                        idExtractor
                    )
                ),
                idExtractor,
            )
        } else {
            return current
        }
    }

    private fun <T : Any, ID : Any> createIdExtractor(repository: R2DBCRepository<T, ID>) = object : Extractor<T, ID> {
        override fun getKey(entity: T): ID {
            return repository.entityManager.getId(entity)
        }
    }
}
