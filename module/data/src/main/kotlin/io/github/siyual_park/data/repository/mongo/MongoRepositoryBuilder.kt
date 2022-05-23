package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.InMemoryStorage
import io.github.siyual_park.data.cache.Pool
import io.github.siyual_park.data.cache.PoolingNestedStorage
import io.github.siyual_park.data.cache.TransactionalStorage
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

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

    fun build(): MongoRepository<T, ID> {
        val cacheBuilder = cacheBuilder

        val current = SimpleMongoRepository<T, ID>(
            template,
            clazz,
            eventPublisher
        )

        return if (cacheBuilder != null) {
            val id = createIdProperty()
            val storage = TransactionalStorage(
                PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, id) }, id)
            )

            CachedMongoRepository(current, storage, id)
        } else {
            return current
        }
    }

    private fun createIdProperty(): WeekProperty<T, ID> {
        val idProperty = (
            clazz.memberProperties.find { it.javaField?.annotations?.find { it is Id } != null }
                ?: throw RuntimeException()
            ) as KProperty1<T, ID>

        return object : WeekProperty<T, ID> {
            override fun get(entity: T): ID {
                return idProperty.get(entity)
            }
        }
    }
}
