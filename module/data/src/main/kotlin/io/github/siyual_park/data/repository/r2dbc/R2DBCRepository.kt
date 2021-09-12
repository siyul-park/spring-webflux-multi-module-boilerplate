package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.annoration.GeneratedValue
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.async
import io.github.siyual_park.data.repository.Repository
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order.asc
import org.springframework.data.domain.Sort.by
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks
import org.springframework.data.mapping.context.MappingContext
import org.springframework.data.projection.SpelAwareProxyProjectionFactory
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.CriteriaDefinition
import org.springframework.data.relational.core.query.Query.empty
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.util.ProxyUtils
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.Parameter
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
open class R2DBCRepository<T : Cloneable<T>, ID : Any>(
    connectionFactory: ConnectionFactory,
    private val clazz: KClass<T>,
    entityCallbacks: ReactiveEntityCallbacks? = null,
    private val scheduler: Scheduler = Schedulers.boundedElastic()
) : Repository<T, ID> {
    private val entityTemplate = R2dbcEntityTemplate(connectionFactory)

    private val databaseClient: DatabaseClient
    private val dataAccessStrategy: ReactiveDataAccessStrategy
    private val mappingContext: MappingContext<out RelationalPersistentEntity<*>, out RelationalPersistentProperty>
    private val projectionFactory: SpelAwareProxyProjectionFactory

    private val idColumn: SqlIdentifier
    private val idProperty: String

    private val generatedValueColumn: Set<SqlIdentifier>

    init {
        if (entityCallbacks != null) {
            entityTemplate.setEntityCallbacks(entityCallbacks)
        }

        val dialect = DialectResolver.getDialect(connectionFactory)

        databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory).bindMarkers(dialect.bindMarkersFactory).build()
        this.dataAccessStrategy = DefaultReactiveDataAccessStrategy(dialect)
        mappingContext = dataAccessStrategy.converter.mappingContext
        projectionFactory = SpelAwareProxyProjectionFactory()

        val persistentEntity = getRequiredEntity(clazz.java)

        idColumn = persistentEntity.requiredIdProperty.columnName
        idProperty = dataAccessStrategy.toSql(idColumn)

        generatedValueColumn = getAnnotatedSqlIdentifier(GeneratedValue::class)
    }

    override suspend fun create(entity: T): T {
        val saved = this.entityTemplate.insert(entity)
            .subscribeOn(scheduler)
            .awaitSingle()

        return this.entityTemplate.select(
            query(where(idProperty).`is`(saved)),
            entity.javaClass
        )
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return flow {
            entities.forEach {
                emit(create(it))
            }
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists(where(idProperty).`is`(id))
    }

    suspend fun exists(criteria: CriteriaDefinition): Boolean {
        return this.entityTemplate.exists(
            query(criteria),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    suspend fun findOneOrFail(criteria: CriteriaDefinition): T {
        return findOne(criteria) ?: throw EmptyResultDataAccessException(1)
    }

    override suspend fun findById(id: ID): T? {
        return findOne(where(idProperty).`is`(id))
    }

    suspend fun findOne(criteria: CriteriaDefinition): T? {
        return this.entityTemplate.selectOne(
            query(criteria),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingleOrNull()
    }

    override fun findAll(): Flow<T> {
        return findAll(criteria = null)
    }

    fun findAll(criteria: CriteriaDefinition? = null, limit: Int? = null, sort: Sort? = null): Flow<T> {
        var query = query(criteria ?: CriteriaDefinition.empty())
        limit?.let {
            query = query.limit(it)
        }
        query = query.sort(sort ?: by(asc(idProperty)))

        return this.entityTemplate.select(
            query,
            clazz.java
        )
            .subscribeOn(scheduler)
            .asFlow()
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        return this.entityTemplate.select(
            query(where(idProperty).`in`(ids.toList()))
                .sort(by(asc(idProperty))),
            clazz.java
        )
            .subscribeOn(scheduler)
            .asFlow()
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override suspend fun updateById(id: ID, patch: AsyncPatch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: AsyncPatch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>, patch: AsyncPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T): T? {
        val originOutboundRow = dataAccessStrategy.getOutboundRow(entity)

        val patch = mutableMapOf<SqlIdentifier, Any>()
        originOutboundRow.forEach { (key, value) ->
            if (!generatedValueColumn.contains(key)) {
                patch[key] = value
            }
        }

        val updateCount = this.entityTemplate.update(
            query(where(idProperty).`is`(originOutboundRow[idColumn])),
            Update.from(patch),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
        if (updateCount == 0) {
            return null
        }

        return findById(originOutboundRow[idColumn].value as ID)
    }

    override suspend fun update(entity: T, patch: AsyncPatch<T>): T? {
        val origin = entity.clone()
        val patched = patch.apply(entity)

        val originOutboundRow = dataAccessStrategy.getOutboundRow(origin)
        val patchedOutboundRow = dataAccessStrategy.getOutboundRow(patched)

        val diff = mutableMapOf<SqlIdentifier, Any>()
        originOutboundRow.keys.forEach {
            val originValue = originOutboundRow[it]
            val patchedValue = patchedOutboundRow[it]

            if (!generatedValueColumn.contains(it) && originValue.value != patchedValue.value) {
                diff[it] = patchedValue
            }
        }

        val updateCount = this.entityTemplate.update(
            query(where(idProperty).`is`(originOutboundRow[idColumn])),
            Update.from(diff),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
        if (updateCount == 0) {
            return null
        }

        return findById(patchedOutboundRow[idColumn].value as ID)
    }

    override suspend fun count(): Long {
        return this.entityTemplate.count(empty(), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override suspend fun deleteById(id: ID) {
        this.entityTemplate.delete(
            query(where(idProperty).`is`(id)),
            clazz.java
        )
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    override suspend fun delete(entity: T) {
        val id = getId(entity).value ?: return
        deleteAll(where(idProperty).`is`(id))
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        deleteAll(where(idProperty).`in`(ids.toList()))
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        val ids = entities.map { getId(it).value }
        deleteAll(where(idProperty).`in`(ids))
    }

    override suspend fun deleteAll() {
        deleteAll(criteria = null)
    }

    suspend fun deleteAll(criteria: CriteriaDefinition? = null) {
        this.entityTemplate.delete(query(criteria ?: CriteriaDefinition.empty()), clazz.java)
            .subscribeOn(scheduler)
            .awaitSingle()
    }

    private fun getId(entity: T): Parameter {
        val outboundRow = dataAccessStrategy.getOutboundRow(entity)
        return outboundRow[idColumn]
    }

    private fun <S : Annotation> getAnnotatedSqlIdentifier(annotationType: KClass<S>): Set<SqlIdentifier> {
        val requiredEntity = getRequiredEntity(clazz.java)

        val generatedValueSqlIdentifier = mutableListOf<SqlIdentifier>()
        requiredEntity.forEach {
            if (it.isAnnotationPresent(annotationType.java)) {
                generatedValueSqlIdentifier.add(it.columnName)
            }
        }

        return generatedValueSqlIdentifier.toSet()
    }

    private fun getRequiredEntity(entity: T): RelationalPersistentEntity<T> {
        val entityType = ProxyUtils.getUserClass(entity)
        return getRequiredEntity(entityType) as RelationalPersistentEntity<T>
    }

    private fun getRequiredEntity(entityClass: Class<*>): RelationalPersistentEntity<*> {
        return mappingContext.getRequiredPersistentEntity(entityClass)
    }
}
