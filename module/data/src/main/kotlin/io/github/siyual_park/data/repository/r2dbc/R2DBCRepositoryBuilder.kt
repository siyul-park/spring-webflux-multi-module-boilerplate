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
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.cache.CachedQueryRepository
import io.github.siyual_park.data.repository.cache.QueryCachedRepository
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import kotlin.reflect.KClass

class R2DBCRepositoryBuilder<T : Any, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
) {
    private val entityManager = EntityManager<T, ID?>(entityOperations, clazz)

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
    fun build(): QueryRepository<T, ID> {
        val idProperty = createIdProperty()

        return R2DBCQueryRepositoryAdapter(
            SimpleR2DBCRepository(
                entityManager,
                eventPublisher
            ),
            clazz
        ).let {
            val cacheBuilder = cacheBuilder
            if (cacheBuilder != null) {
                val storage = TransactionalStorage(
                    PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
                )

                CachedQueryRepository(it, storage, idProperty, clazz)
            } else {
                it
            }
        }.let {
            val cacheBuilder = queryCacheBuilder
            if (cacheBuilder != null) {
                val storage = TransactionalQueryStorage<T>(
                    PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(clazz, cacheBuilder) })
                )

                QueryCachedRepository(it, storage, clazz)
            } else {
                it
            }
        }
    }

    private fun createIdProperty() = object : WeekProperty<T, ID?> {
        override fun get(entity: T): ID? {
            return entityManager.getId(entity)
        }
    }
}
