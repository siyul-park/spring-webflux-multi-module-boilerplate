package io.github.siyual_park.data.repository.r2dbc

import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.data.util.ProxyUtils
import kotlin.reflect.KClass

class EntityManager<T : Any, ID : Any>(
    entityOperations: R2dbcEntityOperations,
    val clazz: KClass<T>,
) {
    private val databaseClient = entityOperations.databaseClient
    private val dataAccessStrategy = entityOperations.dataAccessStrategy
    private val mappingContext = dataAccessStrategy.converter.mappingContext

    val idColumn: SqlIdentifier
    val idProperty: String

    init {
        val persistentEntity = getRequiredEntity(clazz.java)

        idColumn = persistentEntity.requiredIdProperty.columnName
        idProperty = dataAccessStrategy.toSql(idColumn)
    }

    fun getId(row: OutboundRow): ID {
        return row[idColumn].value as ID
    }

    fun getId(entity: T): ID {
        val outboundRow = getOutboundRow(entity)
        return getId(outboundRow)
    }

    fun getOutboundRow(entity: T): OutboundRow {
        return dataAccessStrategy.getOutboundRow(entity)
    }

    fun getRequiredEntity(entity: T): RelationalPersistentEntity<T> {
        val entityType = ProxyUtils.getUserClass(entity)
        return getRequiredEntity(entityType) as RelationalPersistentEntity<T>
    }

    fun getRequiredEntity(entityClass: Class<*>): RelationalPersistentEntity<*> {
        return mappingContext.getRequiredPersistentEntity(entityClass)
    }
}
