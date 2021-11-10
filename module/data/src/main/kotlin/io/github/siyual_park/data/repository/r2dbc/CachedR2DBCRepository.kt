package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.annotation.Unique
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.cache.CachedRepository
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import io.github.siyual_park.data.repository.cache.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
class CachedR2DBCRepository<T : Cloneable<T>, ID : Any> private constructor(
    private val repository: R2DBCRepository<T, ID>,
    cacheBuilder: CacheBuilder<ID, T>,
) : R2DBCRepository<T, ID>,
    CachedRepository<T, ID> by SimpleCachedRepository(
        repository,
        Storage(
            cacheBuilder,
            object : Extractor<T, ID> {
                override fun getKey(entity: T): ID {
                    return repository.entityManager.getId(entity)
                }
            }
        )
    ) {

    override val entityManager: EntityManager<T, ID>
        get() = repository.entityManager

    init {
        val clazz = entityManager.clazz

        clazz.memberProperties.forEach {
            if (it.annotations.any { it is Unique }) {
                storage.createIndex(
                    columnName(it),
                    object : Extractor<T, Any> {
                        override fun getKey(entity: T): Any? {
                            return it.get(entity)
                        }
                    }
                )
            }
        }
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return repository.exists(criteria)
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        if (!criteria.hasPrevious() && !criteria.isGroup && criteria.comparator == CriteriaDefinition.Comparator.EQ) {
            val column = criteria.column
            val value = criteria.value

            val index = column?.reference
            if (value == null || index == null) {
                return repository.findOne(criteria)
                    ?.also { storage.put(it) }
            }

            return storage.getIfPresentAsync(value, index) { repository.findOne(criteria) }
        }

        return repository.findOne(criteria)
            ?.also { storage.put(it) }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return repository.findAll(criteria)
            .onEach { storage.put(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T> {
        return repository.updateAll(criteria, patch)
            .onEach { storage.put(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T> {
        return repository.updateAll(criteria, patch)
            .onEach { storage.put(it) }
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return repository.count(criteria)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?) {
        if (criteria == null) {
            storage.clear()
        } else {
            findAll(criteria)
                .collect { storage.remove(it) }
        }

        repository.deleteAll(criteria)
    }

    companion object {
        fun <T : Cloneable<T>, ID : Any> of(
            repository: R2DBCRepository<T, ID>,
            cacheBuilder: CacheBuilder<Any, Any>
        ): CachedR2DBCRepository<T, ID> {
            return CachedR2DBCRepository(repository, cacheBuilder as CacheBuilder<ID, T>)
        }

        fun <T : Cloneable<T>, ID : Any> of(
            entityOperations: R2dbcEntityOperations,
            clazz: KClass<T>,
            cacheBuilder: CacheBuilder<Any, Any>,
            scheduler: Scheduler = Schedulers.boundedElastic()
        ): CachedR2DBCRepository<T, ID> {
            return CachedR2DBCRepository(
                SimpleR2DBCRepository(entityOperations, clazz, scheduler),
                cacheBuilder as CacheBuilder<ID, T>
            )
        }
    }
}
