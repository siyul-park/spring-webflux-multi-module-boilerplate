package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.data.expansion.idProperty
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.cache.CachedQueryRepository
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class MongoRepositoryBuilder<T : Any, ID : Any>(
    private val template: ReactiveMongoTemplate,
    private val clazz: KClass<T>,
) {
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: (() -> CacheBuilder<Any, Any>)? = null

    fun enableEvent(eventPublisher: EventPublisher?): MongoRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun enableCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): MongoRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
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
                val storage = TransactionalStorage(
                    PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
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
        return object : WeekProperty<T, ID?> {
            override fun get(entity: T): ID? {
                return idProperty.get(entity)
            }
        }
    }
}
