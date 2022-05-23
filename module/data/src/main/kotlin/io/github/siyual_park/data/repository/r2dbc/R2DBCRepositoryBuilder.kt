package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
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
    entityOperations: R2dbcEntityOperations,
    clazz: KClass<T>,
) {
    private val entityManager = EntityManager<T, ID>(entityOperations, clazz)

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

        return SimpleR2DBCRepository(
            entityManager,
            eventPublisher
        ).let {
            val cacheBuilder = cacheBuilder
            if (cacheBuilder != null) {
                val idProperty = createIdProperty()
                val storage = TransactionalStorage(
                    PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
                )

                CachedR2DBCRepository(it, storage, entityManager)
            } else {
                it
            }
        }.let {
            val queryCacheBuilder = queryCacheBuilder
            if (queryCacheBuilder != null) {
                val storage = TransactionalQueryStorage<T>(
                    PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(queryCacheBuilder) })
                )

                CachedQueryR2DBCRepository(it, storage, entityManager)
            } else {
                it
            }
        }
    }

    private fun createIdProperty() = object : WeekProperty<T, ID> {
        override fun get(entity: T): ID {
            return entityManager.getId(entity)
        }
    }
}
