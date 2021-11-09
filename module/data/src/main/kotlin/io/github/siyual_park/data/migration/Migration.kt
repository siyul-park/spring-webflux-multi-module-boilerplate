package io.github.siyual_park.data.migration

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

interface Migration {
    suspend fun up(entityOperations: R2dbcEntityOperations)
    suspend fun down(entityOperations: R2dbcEntityOperations)
}

suspend fun R2dbcEntityOperations.fetchSQL(/*language=SQL*/ sql: String) {
    this.databaseClient.sql(sql)
        .fetch()
        .rowsUpdated()
        .awaitSingle()
}

suspend fun R2dbcEntityOperations.createIndex(tableName: String, columns: Iterable<String>) {
    this.fetchSQL(
        "CREATE INDEX index_${tableName}_${columns.joinToString("_")} " +
            "ON $tableName (${columns.joinToString(", ") { it }})"
    )
}

suspend fun R2dbcEntityOperations.createUniqueIndex(tableName: String, columns: Iterable<String>) {
    this.fetchSQL(
        "CREATE UNIQUE INDEX index_${tableName}_${columns.joinToString("_")} " +
            "ON $tableName (${columns.joinToString(", ") { it }})"
    )
}

suspend fun R2dbcEntityOperations.dropIndex(tableName: String, columns: Iterable<String>) {
    this.fetchSQL(
        "DROP INDEX index_${tableName}_${columns.joinToString("_") { it }} " +
            "ON $tableName"
    )
}

suspend fun R2dbcEntityOperations.dropTable(name: String) {
    this.fetchSQL(
        "DROP TABLE $name"
    )
}

fun R2dbcEntityOperations.isDriver(name: String): Boolean {
    val metadata = databaseClient.connectionFactory.metadata
    return metadata.name == name
}
