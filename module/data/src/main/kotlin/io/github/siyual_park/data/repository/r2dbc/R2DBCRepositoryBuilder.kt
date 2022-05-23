package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.Extractor
import io.github.siyual_park.data.cache.InMemoryQueryStorage
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedQueryStorage
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.TransactionalQueryStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import kotlin.reflect.KClass

class R2DBCRepositoryBuilder<T : Any, ID : Any>(
    private val entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
) {
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: (() -> CacheBuilder<Any, Any>)? = null
    private var queryCacheBuilder: (() -> CacheBuilder<Any, Any>)? = null

    fun enableEvent(eventPublisher: EventPublisher?): R2DBCRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun enableCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): R2DBCRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
        return this
    }

    fun enableQueryCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): R2DBCRepositoryBuilder<T, ID> {
        this.queryCacheBuilder = cacheBuilder
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun build(): R2DBCRepository<T, ID> {
        return SimpleR2DBCRepository<T, ID>(
            entityOperations,
            clazz,
            eventPublisher
        ).let {
            val cacheBuilder = cacheBuilder
            if (cacheBuilder != null) {
                val idExtractor = createIdExtractor(it)
                val storage = TransactionalStorage(
                    PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idExtractor) }, idExtractor)
                )

                CachedR2DBCRepository(it, storage, idExtractor)
            } else {
                it
            }
        }.let {
            val queryCacheBuilder = queryCacheBuilder
            if (queryCacheBuilder != null) {
                val storage = TransactionalQueryStorage<T>(
                    PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(queryCacheBuilder) })
                )

                CachedQueryR2DBCRepository(it, storage)
            } else {
                it
            }
        }
    }

    private fun <T : Any, ID : Any> createIdExtractor(repository: R2DBCRepository<T, ID>) = object : Extractor<T, ID> {
        override fun getKey(entity: T): ID {
            return repository.entityManager.getId(entity)
        }
    }
}
