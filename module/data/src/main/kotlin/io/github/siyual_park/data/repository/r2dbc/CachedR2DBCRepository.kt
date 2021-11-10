package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.repository.cache.CachedRepository
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import io.github.siyual_park.data.repository.cache.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.TreeMap
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
class CachedR2DBCRepository<T : Cloneable<T>, ID : Any>(
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
            if (it.annotations.any { it is Key }) {
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
        if (isSingleCriteria(criteria) && criteria.comparator == CriteriaDefinition.Comparator.EQ) {
            val column = criteria.column
            val value = criteria.value

            val indexName = column?.reference
            if (value == null || indexName == null || !storage.indexNames.contains(indexName)) {
                return repository.findOne(criteria)
                    ?.also { storage.put(it) }
            }

            return storage.getIfPresentAsync(value, indexName) { repository.findOne(criteria) }
        }

        return repository.findOne(criteria)
            ?.also { storage.put(it) }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (isSingleCriteria(criteria) && limit == null && offset == null && sort == null) {
            val column = criteria?.column
            val value = criteria?.value

            val indexName = column?.reference
            if (value == null || indexName == null || !storage.indexNames.contains(indexName)) {
                return repository.findAll(criteria, limit, offset, sort)
                    .onEach { storage.put(it) }
            }

            return when (criteria.comparator) {
                CriteriaDefinition.Comparator.EQ -> flow {
                    storage.getIfPresentAsync(value, indexName) { repository.findOne(criteria) }
                        ?.let { emit(it) }
                }

                CriteriaDefinition.Comparator.IN -> {
                    if (value !is Collection<*>) {
                        repository.findAll(criteria)
                            .onEach { storage.put(it) }
                    } else flow {
                        val result = TreeMap<Int, T>()

                        val notCachedKey = mutableListOf<Pair<Int, *>>()
                        value.forEachIndexed { index, key ->
                            val cached = key?.let { storage.getIfPresent(it, indexName) }
                            if (cached == null) {
                                notCachedKey.add(index to key)
                            } else {
                                result[index] = cached
                            }
                        }

                        repository.findAll(Criteria.where(indexName).`in`(notCachedKey.map { it.second })).toList()
                            .forEachIndexed { index, entity ->
                                val (originIndex, _) = notCachedKey[index]
                                storage.put(entity)
                                result[originIndex] = entity
                            }

                        emitAll(result.values.asFlow())
                    }
                }
                else -> repository.findAll(criteria, limit, offset, sort)
                    .onEach { storage.put(it) }
            }
        }

        return repository.findAll(criteria, limit, offset, sort)
            .onEach { storage.put(it) }
    }

    private fun isSingleCriteria(criteria: CriteriaDefinition?): Boolean {
        return criteria != null && !criteria.hasPrevious() && !criteria.isGroup
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
            cacheBuilder: CacheBuilder<Any, Any> = defaultCacheBuilder()
        ): CachedR2DBCRepository<T, ID> {
            return CachedR2DBCRepository(repository, cacheBuilder as CacheBuilder<ID, T>)
        }

        fun <T : Cloneable<T>, ID : Any> of(
            entityOperations: R2dbcEntityOperations,
            clazz: KClass<T>,
            cacheBuilder: CacheBuilder<Any, Any> = defaultCacheBuilder(),
            scheduler: Scheduler = Schedulers.boundedElastic()
        ): CachedR2DBCRepository<T, ID> {
            return CachedR2DBCRepository(
                SimpleR2DBCRepository(entityOperations, clazz, scheduler),
                cacheBuilder as CacheBuilder<ID, T>
            )
        }

        private fun defaultCacheBuilder() = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)
    }
}
