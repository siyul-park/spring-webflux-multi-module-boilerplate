package io.github.siyual_park.data.repository

import io.github.siyual_park.data.Cloneable
import io.github.siyual_park.data.patch.Patch
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort.Order.asc
import org.springframework.data.domain.Sort.by
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
import reactor.core.publisher.Flux
import kotlin.reflect.KClass

@Suppress("NULLABLE_TYPE_PARAMETER_AGAINST_NOT_NULL_TYPE_PARAMETER", "UNCHECKED_CAST")
class SimpleRepository<T: Cloneable<T>, ID: Any>(
    connectionFactory: ConnectionFactory,
    private val clazz: KClass<T>
) {
    private val entityTemplate = R2dbcEntityTemplate(connectionFactory)

    private val databaseClient: DatabaseClient
    private val dataAccessStrategy: ReactiveDataAccessStrategy
    private val mappingContext: MappingContext<out RelationalPersistentEntity<*>, out RelationalPersistentProperty>
    private val projectionFactory: SpelAwareProxyProjectionFactory

    private val idColumn: SqlIdentifier
    private val idProperty: String

    init {
        val dialect = DialectResolver.getDialect(connectionFactory)

        databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory).bindMarkers(dialect.bindMarkersFactory).build()
        this.dataAccessStrategy = DefaultReactiveDataAccessStrategy(dialect)
        mappingContext = dataAccessStrategy.converter.mappingContext
        projectionFactory = SpelAwareProxyProjectionFactory()

        val persistentEntity = getRequiredEntity(clazz.java)

        idColumn = persistentEntity.requiredIdProperty.columnName
        idProperty = dataAccessStrategy.toSql(idColumn)
    }
    suspend fun <S : T> create(entity: S): S {
        return this.entityTemplate.insert(entity)
            .awaitSingle()
    }

    fun <S : T> createAll(entities: Iterable<S>): Flow<S> {
        return Flux.fromIterable(entities)
            .flatMap { this.entityTemplate.insert(it) }
            .asFlow()
    }

    suspend fun findById(id: ID): T? {
        return this.entityTemplate.selectOne(
            query(where(idProperty).`is`(id)),
            clazz.java
        ).awaitSingleOrNull()
    }

    suspend fun existsById(id: ID): Boolean {
        return this.entityTemplate.exists(
            query(where(idProperty).`is`(id)),
            clazz.java
        ).awaitSingle()
    }

    fun findAll(): Flow<T> {
        return this.entityTemplate.select(
            query(CriteriaDefinition.empty())
                .sort(by(asc(idProperty))),
            clazz.java
        )
            .asFlow()
    }

    fun findAllById(ids: Iterable<ID>): Flow<T> {
        return this.entityTemplate.select(
            query(where(idProperty).`in`(ids))
                .sort(by(asc(idProperty))),
            clazz.java
        )
            .asFlow()
    }

    suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return findById(id)
            ?.let { update(it, patch) }
    }

    suspend fun update(entity: T, patch: Patch<T>): T? {
        val origin = entity.clone()
        val patched = patch.apply(entity)

        val originOutboundRow = dataAccessStrategy.getOutboundRow(origin)
        val patchedOutboundRow = dataAccessStrategy.getOutboundRow(patched)

        val diff = mutableMapOf<SqlIdentifier, Any>()
        originOutboundRow.keys.forEach {
            val originValue = originOutboundRow[it]
            val patchedValue = patchedOutboundRow[it]

            if (originValue.value != patchedValue.value) {
                diff[it] = patchedValue
            }
        }

        val update_count = this.entityTemplate.update(
            query(where(idProperty).`is`(originOutboundRow[idColumn])),
            Update.from(diff),
            clazz.java
        ).awaitSingle()
        if (update_count == 0) {
            return null
        }

        return this.entityTemplate.selectOne(
            query(where(idProperty).`is`(patchedOutboundRow[idColumn])),
            clazz.java
        ).awaitSingleOrNull()
    }

    suspend fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    suspend fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    suspend fun count(): Long {
        return this.entityTemplate.count(empty(), clazz.java)
            .awaitSingle()
    }

    suspend fun deleteById(id: ID) {
        this.entityTemplate.delete(
            query(where(idProperty).`is`(id)),
            clazz.java
        ).awaitSingle()
    }

    suspend fun delete(entity: T) {
        this.entityTemplate.delete(
            query(where(idProperty).`is`(getId(entity))),
            clazz.java
        ).awaitSingle()
    }

    suspend fun deleteAllById(ids: Iterable<ID>) {
        this.entityTemplate.delete(
            query(where(idProperty).`in`(ids)),
            clazz.java
        ).awaitSingle()
    }

    suspend fun deleteAll(entities: Iterable<T>) {
        this.entityTemplate.delete(
            query(where(idProperty).`in`(entities.map { getId(it) })),
            clazz.java
        ).awaitSingle()
    }

    suspend fun deleteAll() {
        this.entityTemplate.delete(empty(), clazz.java)
            .awaitSingle()
    }

    private fun getId(entity: T): Parameter {
        val outboundRow = dataAccessStrategy.getOutboundRow(entity)
        return outboundRow[idColumn]
    }

    private fun <T> getRequiredEntity(entity: T): RelationalPersistentEntity<T> {
        val entityType = ProxyUtils.getUserClass(entity as Any)
        return getRequiredEntity(entityType) as RelationalPersistentEntity<T>
    }

    private fun getRequiredEntity(entityClass: Class<*>): RelationalPersistentEntity<*> {
        return mappingContext.getRequiredPersistentEntity(entityClass)
    }
}