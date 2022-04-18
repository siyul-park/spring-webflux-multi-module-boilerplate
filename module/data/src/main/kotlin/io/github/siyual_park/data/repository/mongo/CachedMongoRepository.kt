package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.InMemoryNestedStorage
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import io.github.siyual_park.data.repository.cache.TransactionalStorageManager
import io.github.siyual_park.data.repository.cache.createIndexes
import io.github.siyual_park.event.EventPublisher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("UNCHECKED_CAST")
class CachedMongoRepository<T : Any, ID : Any>(
    private val delegator: MongoRepository<T, ID>,
    private val storageManager: TransactionalStorageManager<T, ID>,
    private val idExtractor: Extractor<T, ID>,
) : MongoRepository<T, ID>,
    Repository<T, ID> by SimpleCachedRepository(
        delegator,
        storageManager,
        idExtractor,
    ) {

    override val template: ReactiveMongoTemplate
        get() = delegator.template
    override val clazz: KClass<T>
        get() = delegator.clazz

    init {
        storageManager.createIndexes(clazz)
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        val storage = storageManager.getCurrent()

        val fallback = suspend { delegator.exists(criteria) }
        val (indexName, value) = getIndexNameAndValue(criteria) ?: return fallback()

        return if (storage.getIfPresent(indexName, value) != null) {
            true
        } else {
            fallback()
        }
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        val storage = storageManager.getCurrent()

        val fallback = suspend {
            delegator.findOne(criteria)
                ?.also { storage.put(it) }
        }

        val (indexName, value) = getIndexNameAndValue(criteria) ?: return fallback()
        return storage.getIfPresentAsync(indexName, value) { delegator.findOne(criteria) }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()

            val fallback = {
                delegator.findAll(criteria, limit, offset, sort)
                    .onEach { storage.put(it) }
            }

            if (criteria != null && limit == null && offset == null && sort == null) {
                val indexNameAndValue = getIndexNameAndValue(criteria)
                if (indexNameAndValue != null) {
                    val (indexName, value) = indexNameAndValue
                    storage.getIfPresentAsync(indexName, value) { delegator.findOne(criteria) }
                        ?.let { emit(it) }
                    return@flow
                }

                if (isSingleCriteria(criteria)) {
                    val document = criteria.criteriaObject
                    val column = criteria.key ?: return@flow emitAll(fallback())
                    val child = document[column] ?: return@flow emitAll(fallback())
                    if (child !is Document) {
                        return@flow emitAll(fallback())
                    }
                    if (child.size != 1 || child.keys.toList()[0] != "\$in") {
                        return@flow emitAll(fallback())
                    }
                    val value = child["\$in"]

                    if (
                        storage.containsIndex(column) &&
                        value is Collection<*>
                    ) {
                        val result = mutableListOf<T>()
                        val notCachedKey = mutableListOf<Any?>()
                        value.forEach { key ->
                            val cached = key?.let { storage.getIfPresent(column, it) }
                            if (cached == null) {
                                notCachedKey.add(key)
                            } else {
                                result.add(cached)
                            }
                        }

                        if (notCachedKey.isNotEmpty()) {
                            delegator.findAll(Criteria.where(column).`in`(notCachedKey))
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

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        val storage = storageManager.getCurrent()

        return delegator.update(criteria, patch)
            ?.also { storage.put(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>): Flow<T> {
        return updateAll(criteria, patch.async())
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()

            emitAll(
                delegator.updateAll(criteria, patch)
                    .onEach { storage.put(it) }
            )
        }
    }

    override suspend fun count(criteria: CriteriaDefinition?): Long {
        return delegator.count(criteria)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        val storage = storageManager.getCurrent()

        if (criteria == null) {
            storage.clear()
            delegator.deleteAll()
        } else {
            val founded = findAll(criteria, limit, offset, sort)
                .onEach { storage.delete(it) }
                .toList()

            delegator.deleteAll(founded)
        }
    }

    private suspend fun getIndexNameAndValue(criteria: CriteriaDefinition?): Pair<String, Any>? {
        val storage = storageManager.getCurrent()

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

        val document = criteria.criteriaObject

        document.entries.forEach { (key, value) ->
            if (key.startsWith("$")) {
                return null
            }

            columns.add(key)
            values.add(value)
        }

        return columns to values
    }

    private fun isSingleCriteria(criteria: CriteriaDefinition?): Boolean {
        return criteria != null && criteria.criteriaObject.size == 1 && criteria.key != null
    }

    companion object {
        fun <T : Any, ID : Any> of(
            repository: MongoRepository<T, ID>,
            cacheBuilder: CacheBuilder<Any, Any> = defaultCacheBuilder(),
        ): CachedMongoRepository<T, ID> {
            val idExtractor = createIdExtractor(repository)

            return CachedMongoRepository(
                repository,
                TransactionalStorageManager(
                    InMemoryNestedStorage(
                        cacheBuilder as CacheBuilder<ID, T>,
                        idExtractor
                    )
                ),
                idExtractor,
            )
        }

        fun <T : Any, ID : Any> of(
            template: ReactiveMongoTemplate,
            clazz: KClass<T>,
            cacheBuilder: CacheBuilder<Any, Any> = defaultCacheBuilder(),
            scheduler: Scheduler = Schedulers.boundedElastic(),
            eventPublisher: EventPublisher? = null
        ): CachedMongoRepository<T, ID> {
            val repository = SimpleMongoRepository<T, ID>(template, clazz, scheduler, eventPublisher)
            val idExtractor = createIdExtractor(repository)

            return CachedMongoRepository(
                repository,
                TransactionalStorageManager(
                    InMemoryNestedStorage(
                        cacheBuilder as CacheBuilder<ID, T>,
                        idExtractor
                    )
                ),
                idExtractor,
            )
        }

        private fun defaultCacheBuilder() = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterAccess(Duration.ofMinutes(2))
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1_000)

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
}
