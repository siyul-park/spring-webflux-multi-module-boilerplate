package io.github.siyual_park.data.repository.r2dbc

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.expansion.columnName
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.cache.CachedRepository
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.cache.NestedStorage
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import io.github.siyual_park.data.repository.cache.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
class CachedR2DBCRepository<T : Any, ID : Any>(
    private val repository: R2DBCRepository<T, ID>,
    override val storage: Storage<T, ID>,
    private val idExtractor: Extractor<T, ID>
) : R2DBCRepository<T, ID>,
    CachedRepository<T, ID> by SimpleCachedRepository(
        repository,
        storage,
        idExtractor
    ) {

    private val cachedTransactionStorageManager = CachedTransactionStorageManager(storage as NestedStorage<T, ID>)

    override val entityManager: EntityManager<T, ID>
        get() = repository.entityManager

    init {
        val clazz = entityManager.clazz

        val indexes = mutableMapOf<String, MutableList<KProperty1<T, *>>>()

        clazz.memberProperties.forEach {
            val index = it.annotations.find { it is Key } as? Key ?: return@forEach
            indexes.getOrPut(index.name.ifEmpty { columnName(it) }) { mutableListOf() }
                .add(it)
        }

        indexes.forEach { (_, properties) ->
            val sortedProperties = properties.map { it to columnName(it) }.sortedBy { (_, columnName) -> columnName }
            storage.createIndex(
                sortedProperties.joinToString(" ") { (_, columnName) -> columnName },
                object : Extractor<T, Any> {
                    override fun getKey(entity: T): Any {
                        val key = ArrayList<Any?>()
                        sortedProperties.forEach { (property, _) -> key.add(property.get(entity)) }
                        return key
                    }
                }
            )
        }
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        val storage = cachedTransactionStorageManager.getCurrent()

        val fallback = suspend { repository.exists(criteria) }
        val (indexName, value) = getIndexNameAndValue(criteria) ?: return fallback()

        return if (storage.getIfPresent(value, indexName) != null) {
            true
        } else {
            fallback()
        }
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        val storage = cachedTransactionStorageManager.getCurrent()

        val fallback = suspend {
            repository.findOne(criteria)
                ?.also { storage.put(it) }
        }

        val (indexName, value) = getIndexNameAndValue(criteria) ?: return fallback()
        return storage.getIfPresentAsync(value, indexName) { repository.findOne(criteria) }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return flow {
            val storage = cachedTransactionStorageManager.getCurrent()

            val fallback = {
                repository.findAll(criteria, limit, offset, sort)
                    .onEach { storage.put(it) }
            }

            if (criteria != null && limit == null && offset == null && sort == null) {
                val indexNameAndValue = getIndexNameAndValue(criteria)
                if (indexNameAndValue != null) {
                    val (indexName, value) = indexNameAndValue
                    storage.getIfPresentAsync(value, indexName) { repository.findOne(criteria) }
                        ?.let { emit(it) }
                    return@flow
                }

                if (isSingleCriteria(criteria)) {
                    val column = criteria.column
                    val value = criteria.value ?: return@flow emitAll(fallback())
                    val indexName = column?.reference ?: return@flow emitAll(fallback())

                    if (
                        storage.containsIndex(indexName) &&
                        criteria.comparator == CriteriaDefinition.Comparator.IN &&
                        value is Collection<*>
                    ) {
                        val result = mutableListOf<T>()
                        val notCachedKey = mutableListOf<Any?>()
                        value.forEach { key ->
                            val cached = key?.let { storage.getIfPresent(it, indexName) }
                            if (cached == null) {
                                notCachedKey.add(key)
                            } else {
                                result.add(cached)
                            }
                        }

                        if (notCachedKey.isNotEmpty()) {
                            repository.findAll(Criteria.where(indexName).`in`(notCachedKey))
                                .collect { entity ->
                                    storage.put(entity)
                                    result.add(entity)
                                }
                        }

                        return@flow emitAll(result.asFlow())
                    }
                }
            }

            return@flow emitAll(fallback())
        }
    }

    private suspend fun getIndexNameAndValue(criteria: CriteriaDefinition?): Pair<String, Any>? {
        val storage = cachedTransactionStorageManager.getCurrent()

        if (criteria == null) return null

        val columnsAndValues = getSimpleJoinedColumnsAndValues(criteria) ?: return null
        val (columns, values) = columnsAndValues
        val sorted = columns.mapIndexed { index, column -> column to values[index] }
            .sortedBy { (column, _) -> column }

        val indexName = sorted.joinToString(" ") { (column, _) -> column }
        val value = ArrayList<Any?>()
        sorted.forEach { (_, it) -> value.add(it) }

        if (!storage.containsIndex(indexName)) {
            return null
        }

        return indexName to value
    }

    private fun getSimpleJoinedColumnsAndValues(criteria: CriteriaDefinition): Pair<MutableList<String>, MutableList<Any?>>? {
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()

        when {
            criteria.isGroup -> {
                if (criteria.combinator != CriteriaDefinition.Combinator.AND) {
                    return null
                }

                criteria.group.forEach {
                    val (childColumns, childValues) = getSimpleJoinedColumnsAndValues(it) ?: return null
                    columns.addAll(childColumns)
                    values.addAll(childValues)
                }
            }
            criteria.comparator === CriteriaDefinition.Comparator.EQ -> {
                val column = criteria.column
                val value = criteria.value
                val indexName = column?.reference ?: return null

                columns.add(indexName)
                values.add(value)
            }
            else -> return null
        }

        val previous = criteria.previous
        if (previous != null) {
            val (childColumns, childValues) = getSimpleJoinedColumnsAndValues(previous) ?: return null
            columns.addAll(childColumns)
            values.addAll(childValues)
        }

        return columns to values
    }

    private fun isSingleCriteria(criteria: CriteriaDefinition?): Boolean {
        return criteria != null && !criteria.hasPrevious() && !criteria.isGroup
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        val storage = cachedTransactionStorageManager.getCurrent()

        return repository.update(criteria, patch)
            ?.also { storage.put(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T> {
        return updateAll(criteria, patch.async())
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T> {
        return flow {
            val storage = cachedTransactionStorageManager.getCurrent()

            emitAll(
                repository.updateAll(criteria, patch)
                    .onEach { storage.put(it) }
            )
        }
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return repository.count(criteria)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?) {
        val storage = cachedTransactionStorageManager.getCurrent()

        if (criteria == null) {
            storage.clear()
        } else {
            findAll(criteria)
                .collect { storage.delete(it) }
        }

        repository.deleteAll(criteria)
    }

    companion object {
        fun <T : Any, ID : Any> of(
            repository: R2DBCRepository<T, ID>,
            cacheBuilder: () -> CacheBuilder<Any, Any> = { defaultCacheBuilder() }
        ): CachedR2DBCRepository<T, ID> {
            val idExtractor = createIdExtractor(repository)

            return CachedR2DBCRepository(
                repository,
                InMemoryNestedStorage(
                    cacheBuilder as () -> CacheBuilder<ID, T>,
                    idExtractor
                ),
                idExtractor
            )
        }

        fun <T : Any, ID : Any> of(
            entityOperations: R2dbcEntityOperations,
            clazz: KClass<T>,
            cacheBuilder: () -> CacheBuilder<Any, Any> = { defaultCacheBuilder() },
            scheduler: Scheduler = Schedulers.boundedElastic()
        ): CachedR2DBCRepository<T, ID> {
            val repository = SimpleR2DBCRepository<T, ID>(entityOperations, clazz, scheduler)
            val idExtractor = createIdExtractor(repository)

            return CachedR2DBCRepository(
                repository,
                InMemoryNestedStorage(
                    cacheBuilder as () -> CacheBuilder<ID, T>,
                    idExtractor
                ),
                idExtractor
            )
        }

        private fun defaultCacheBuilder() = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(2))
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)

        private fun <T : Any, ID : Any> createIdExtractor(repository: R2DBCRepository<T, ID>) = object : Extractor<T, ID> {
            override fun getKey(entity: T): ID {
                return repository.entityManager.getId(entity)
            }
        }
    }
}
