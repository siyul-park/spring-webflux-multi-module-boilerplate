package io.github.siyual_park.data.repository.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.CaseFormat
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.InMemoryQueryStorage
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.MultiLevelNestedStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedQueryStorage
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.RedisStorage
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.data.cache.TransactionalQueryStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.data.repository.QueryableRepository
import io.github.siyual_park.data.repository.cache.CachedQueryableRepository
import io.github.siyual_park.data.repository.cache.QueryCachedRepository
import io.github.siyual_park.event.EventPublisher
import org.redisson.api.RedissonClient
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.mapping.Table
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class R2DBCRepositoryBuilder<T : Any, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
) {
    private val entityManager = EntityManager<T, ID?>(entityOperations, clazz)

    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: (() -> CacheBuilder<Any, Any>)? = null
    private var queryCacheBuilder: (() -> CacheBuilder<Any, Any>)? = null

    private var cacheStorageManager: StorageManager? = null

    private var redisClient: RedissonClient? = null
    private var expiredAt: WeekProperty<T, Instant?>? = null
    private var size: Int? = null

    private var objectMapper: ObjectMapper? = null

    fun enableJsonMapping(objectMapper: ObjectMapper?): R2DBCRepositoryBuilder<T, ID> {
        this.objectMapper = objectMapper
        return this
    }

    fun enableEvent(eventPublisher: EventPublisher?): R2DBCRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun enableCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): R2DBCRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
        return this
    }

    fun enableCache(redisClient: RedissonClient?, expiredAt: WeekProperty<T, Instant?>?, size: Int?): R2DBCRepositoryBuilder<T, ID> {
        this.redisClient = redisClient
        this.expiredAt = expiredAt
        this.size = size
        return this
    }

    fun enableQueryCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): R2DBCRepositoryBuilder<T, ID> {
        this.queryCacheBuilder = cacheBuilder
        return this
    }

    fun enableCacheStorageManager(cacheStorageManager: StorageManager?): R2DBCRepositoryBuilder<T, ID> {
        this.cacheStorageManager = cacheStorageManager
        return this
    }

    fun build(): QueryableRepository<T, ID> {
        return applyQueryCache(
            applyCache(
                R2DBCQueryableRepositoryAdapter(SimpleR2DBCRepository(entityManager, eventPublisher), clazz)
            )
        )
    }

    private fun applyCache(repository: QueryableRepository<T, ID>): QueryableRepository<T, ID> {
        val cacheBuilder = cacheBuilder ?: return repository
        val idProperty = createIdProperty()
        val redisClient = redisClient

        val storage = TransactionalStorage(
            if (redisClient != null) {
                MultiLevelNestedStorage(
                    RedisStorage(
                        redisClient,
                        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.simpleName ?: ""),
                        size = size ?: 1024,
                        objectMapper = objectMapper ?: jacksonObjectMapper(),
                        id = idProperty,
                        expiredAt = expiredAt ?: WeekProperty { Instant.now().plus(Duration.ofMinutes(1)) },
                        keyClass = entityManager.getIdProperty().returnType.classifier as KClass<ID>,
                        valueClass = clazz,
                    ),
                    Pool { InMemoryStorage(cacheBuilder, idProperty) },
                    idProperty
                )
            } else {
                PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
            }
        )

        clazz.annotations.find { it is Table }?.let {
            cacheStorageManager?.put((it as Table).value, storage)
        }

        return CachedQueryableRepository(repository, storage, idProperty, clazz)
    }

    private fun applyQueryCache(repository: QueryableRepository<T, ID>): QueryableRepository<T, ID> {
        val cacheBuilder = queryCacheBuilder ?: return repository
        val storage = TransactionalQueryStorage(
            PoolingNestedQueryStorage(Pool { InMemoryQueryStorage(clazz, cacheBuilder) })
        )

        return QueryCachedRepository(repository, storage, clazz)
    }

    private fun createIdProperty() = WeekProperty<T, ID?> { entity -> entityManager.getId(entity) }
}
