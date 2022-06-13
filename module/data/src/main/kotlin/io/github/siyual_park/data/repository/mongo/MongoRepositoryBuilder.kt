package io.github.siyual_park.data.repository.mongo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.CaseFormat
import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.MultiLevelNestedStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.RedisStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.data.expansion.idProperty
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.cache.CachedQueryRepository
import io.github.siyual_park.event.EventPublisher
import org.redisson.api.RedissonClient
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class MongoRepositoryBuilder<T : Any, ID : Any>(
    private val template: ReactiveMongoTemplate,
    private val clazz: KClass<T>,
) {
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: (() -> CacheBuilder<Any, Any>)? = null

    private var redisClient: RedissonClient? = null
    private var expiredAt: WeekProperty<T, Instant?>? = null
    private var size: Int? = null

    private var objectMapper: ObjectMapper? = null

    fun enableJsonMapping(objectMapper: ObjectMapper?): MongoRepositoryBuilder<T, ID> {
        this.objectMapper = objectMapper
        return this
    }

    fun enableEvent(eventPublisher: EventPublisher?): MongoRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun enableCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): MongoRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
        return this
    }

    fun enableCache(redisClient: RedissonClient?, expiredAt: WeekProperty<T, Instant?>?, size: Int?): MongoRepositoryBuilder<T, ID> {
        this.redisClient = redisClient
        this.expiredAt = expiredAt
        this.size = size
        return this
    }

    fun build(): QueryRepository<T, ID> {
        val idProperty = createIdProperty()

        val current = MongoQueryRepositoryAdapter(
            SimpleMongoRepository<T, ID>(
                template,
                clazz,
                eventPublisher
            ),
            clazz
        ).let {
            val cacheBuilder = cacheBuilder
            if (cacheBuilder != null) {
                val redisClient = redisClient
                val storage = TransactionalStorage(
                    if (redisClient != null) {
                        MultiLevelNestedStorage(
                            RedisStorage(
                                redisClient,
                                name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.simpleName ?: ""),
                                size = size ?: 1000,
                                objectMapper = objectMapper ?: jacksonObjectMapper(),
                                id = idProperty,
                                expiredAt = expiredAt ?: WeekProperty { Instant.now().plus(Duration.ofMinutes(1)) },
                                keyClass = idProperty<T, ID?>(clazz).returnType.classifier as KClass<ID>,
                                valueClass = clazz,
                            ),
                            Pool { InMemoryStorage(cacheBuilder, idProperty) },
                            idProperty
                        )
                    } else {
                        PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
                    }
                )
                CachedQueryRepository(it, storage, idProperty, clazz)
            } else {
                it
            }
        }

        return current
    }

    private fun createIdProperty(): WeekProperty<T, ID?> {
        val idProperty = idProperty<T, ID?>(clazz)
        return WeekProperty { entity -> idProperty.get(entity) }
    }
}
