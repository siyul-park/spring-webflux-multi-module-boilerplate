package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.cache.Extractor
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import io.github.siyual_park.data.repository.cache.TransactionalStorageManager
import io.github.siyual_park.data.repository.cache.createIndexes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Update
import kotlin.reflect.KClass

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
                            val cached = key?.let { storage.getIfPresent(column, ArrayList<Any?>().apply { add(it) }) }
                            if (cached == null) {
                                notCachedKey.add(key)
                            } else {
                                result.add(cached)
                            }
                        }

                        if (notCachedKey.isNotEmpty()) {
                            delegator.findAll(Criteria.where(column).`in`(notCachedKey))
                                .onEach { storage.put(it) }
                                .collect { result.add(it) }
                        }

                        return@flow emitAll(result.asFlow())
                    }
                }
            }

            return@flow emitAll(fallback())
        }
    }

    override suspend fun update(criteria: CriteriaDefinition, update: Update): T? {
        val storage = storageManager.getCurrent()

        return delegator.update(criteria, update)
            ?.also { storage.put(it) }
    }

    override suspend fun update(entity: T, update: Update): T? {
        val storage = storageManager.getCurrent()

        return delegator.update(entity, update)
            ?.also { storage.put(it) }
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: CriteriaDefinition, patch: AsyncPatch<T>): T? {
        val storage = storageManager.getCurrent()

        return delegator.update(criteria, patch)
            ?.also { storage.put(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: AsyncPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return flow {
            val storage = storageManager.getCurrent()

            emitAll(
                delegator.updateAll(criteria, patch, limit, offset, sort)
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
            if (value is Document) {
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
}
