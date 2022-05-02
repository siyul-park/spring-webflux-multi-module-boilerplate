package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.cache.TransactionalStorageManager
import io.github.siyual_park.event.EventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KClass

class R2DBCRepositoryBuilder<T : Any, ID : Any>(
    private val entityOperations: R2dbcEntityOperations,
    private val clazz: KClass<T>,
) {
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: CacheBuilder<Any, Any>? = null
    private var filter: (() -> Criteria)? = null
    private var softDelete: Boolean = false

    fun set(eventPublisher: EventPublisher?): R2DBCRepositoryBuilder<T, ID> {
        this.eventPublisher = eventPublisher
        return this
    }

    fun set(cacheBuilder: CacheBuilder<Any, Any>?): R2DBCRepositoryBuilder<T, ID> {
        this.cacheBuilder = cacheBuilder
        return this
    }

    fun set(filter: (() -> Criteria)?): R2DBCRepositoryBuilder<T, ID> {
        this.filter = filter
        return this
    }

    fun softDelete(softDelete: Boolean): R2DBCRepositoryBuilder<T, ID> {
        this.softDelete = softDelete
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun build(): R2DBCRepository<T, ID> {
        val filter = filter
        val cacheBuilder = cacheBuilder
        val softDelete = softDelete

        val current = if (filter != null) {
            FilteredR2DBCRepository(
                entityOperations,
                clazz,
                filter,
                eventPublisher
            )
        } else if (softDelete) {
            SoftDeletedR2DBCRepository<SoftDeletable, ID>(
                entityOperations,
                clazz as KClass<SoftDeletable>,
                eventPublisher
            ) as R2DBCRepository<T, ID>
        } else {
            SimpleR2DBCRepository(
                entityOperations,
                clazz,
                eventPublisher
            )
        }

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
