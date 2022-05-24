package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.WeekProperty
import io.github.siyual_park.data.cache.Storage
import io.github.siyual_park.data.cache.createIndexes
import io.github.siyual_park.data.cache.getIndexNameAndValue
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.github.siyual_park.data.repository.cache.SimpleCachedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.CriteriaDefinition

@Suppress("UNCHECKED_CAST")
class CachedR2DBCRepository<T : Any, ID : Any>(
    private val delegator: R2DBCRepository<T, ID>,
    private val storage: Storage<ID, T>,
    private val entityManager: EntityManager<T, ID>,
) : R2DBCRepository<T, ID>,
    Repository<T, ID> by SimpleCachedRepository(
        delegator,
        storage,
        object : WeekProperty<T, ID> {
            override fun get(entity: T): ID {
                return entityManager.getId(entity)
            }
        },
    ) {

    init {
        runBlocking { storage.createIndexes(entityManager.getClass()) }
    }

    override suspend fun exists(criteria: CriteriaDefinition): Boolean {
        val fallback = suspend { delegator.exists(criteria) }
        val (indexName, value) = getUniqueIndexNameAndValue(criteria) ?: return fallback()

        return if (storage.getIfPresent(indexName, value) != null) {
            true
        } else {
            fallback()
        }
    }

    override suspend fun findOne(criteria: CriteriaDefinition): T? {
        val fallback = suspend {
            delegator.findOne(criteria)
                ?.also { storage.add(it) }
        }

        val (indexName, value) = getUniqueIndexNameAndValue(criteria) ?: return fallback()
        return storage.getIfPresent(indexName, value) { delegator.findOne(criteria) }
    }

    override fun findAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }

        return flow {
            val fallback = {
                delegator.findAll(criteria, limit, offset, sort)
                    .onEach { storage.add(it) }
            }

            if (criteria != null && (offset == null || offset == 0L)) {
                val indexNameAndValue = getUniqueIndexNameAndValue(criteria)
                if (indexNameAndValue != null) {
                    val (indexName, value) = indexNameAndValue
                    storage.getIfPresent(indexName, value) { delegator.findOne(criteria) }
                        ?.let { emit(it) }
                    return@flow
                }
            }

            if (criteria != null && limit == null && offset == null && sort == null) {
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
                            val cached = key?.let { storage.getIfPresent(indexName, ArrayList<Any?>().apply { add(it) }) }
                            if (cached == null) {
                                notCachedKey.add(key)
                            } else {
                                result.add(cached)
                            }
                        }

                        if (notCachedKey.isNotEmpty()) {
                            delegator.findAll(Criteria.where(indexName).`in`(notCachedKey))
                                .onEach { storage.add(it) }
                                .collect { result.add(it) }
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

    override suspend fun update(criteria: CriteriaDefinition, patch: SuspendPatch<T>): T? {
        return delegator.update(criteria, patch)
            ?.also { storage.add(it) }
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(criteria: CriteriaDefinition, patch: SuspendPatch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        return flow {
            emitAll(
                delegator.updateAll(criteria, patch, limit, offset, sort)
                    .onEach { storage.add(it) }
            )
        }
    }

    override suspend fun count(criteria: CriteriaDefinition?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }
        return delegator.count(criteria, limit)
    }

    override suspend fun deleteAll(criteria: CriteriaDefinition?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }
        if (criteria == null) {
            storage.clear()
            delegator.deleteAll()
        } else {
            val founded = findAll(criteria, limit, offset, sort)
                .onEach { entityManager.getId(it).let { id -> storage.remove(id) } }
                .toList()

            delegator.deleteAll(founded)
        }
    }

    private suspend fun getUniqueIndexNameAndValue(criteria: CriteriaDefinition?): Pair<String, Any>? {
        if (criteria == null) return null

        val columnsAndValues = getSimpleJoinedColumnsAndValues(criteria) ?: return null
        val (columns, values) = columnsAndValues

        return storage.getIndexNameAndValue(columns, values)
    }

    private fun getSimpleJoinedColumnsAndValues(criteria: CriteriaDefinition): Pair<MutableList<String>, MutableList<Any?>>? {
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()

        when {
            criteria.isGroup -> {
                if (criteria.combinator == CriteriaDefinition.Combinator.INITIAL && criteria.group.size == 1) {
                    return getSimpleJoinedColumnsAndValues(criteria.group[0])
                }
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
                val indexName = column?.let { entityManager.getProperty(it)?.name } ?: return null

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
}
