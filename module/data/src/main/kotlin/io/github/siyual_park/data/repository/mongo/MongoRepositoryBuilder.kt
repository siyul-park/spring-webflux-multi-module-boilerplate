package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.cache.InMemoryStorage
import io.github.siyual_park.data.repository.cache.TransactionalStorageManager
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
    private var cacheBuilder: CacheBuilder<Any, Any>? = null

    fun set(eventPublisher: EventPublisher?): MongoRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun set(cacheBuilder: CacheBuilder<Any, Any>?): MongoRepositoryBuilder<T, ID> {
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
            val idExtractor = createIdExtractor(current)
            val transactionalStorageManager = TransactionalStorageManager(
                InMemoryNestedStorage(
                    InMemoryStorage(
                        cacheBuilder as CacheBuilder<ID, T>,
                        idExtractor
                    )
                )
            )

            CachedMongoRepository(
                current,
                transactionalStorageManager,
                idExtractor,
            )
        } else {
            return current
        }
    }

    private fun <T : Any, ID : Any> createIdExtractor(repository: MongoRepository<T, ID>): Extractor<T, ID> {
        val idProperty = (
            repository.clazz.memberProperties.find { it.javaField?.annotations?.find { it is Id } != null }
                ?: throw RuntimeException()
            ) as KProperty1<T, ID>

        return object : Extractor<T, ID> {
            override fun getKey(entity: T): ID {
                return idProperty.get(entity)
            }
        }
    }
}
