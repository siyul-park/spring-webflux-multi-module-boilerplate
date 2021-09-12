package io.github.siyual_park.data.migration

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

interface Migration {
    suspend fun up(entityTemplate: R2dbcEntityTemplate)
    suspend fun down(entityTemplate: R2dbcEntityTemplate)
}

suspend fun R2dbcEntityTemplate.createIndex(tableName: String, columns: Iterable<String>) {
    this.databaseClient.sql(
        "CREATE INDEX index_${tableName}_${columns.joinToString("_")} " +
            "ON $tableName (${columns.joinToString(",")})"
    )
        .fetch()
        .rowsUpdated()
        .awaitSingle()
}

suspend fun R2dbcEntityTemplate.createUniqueIndex(tableName: String, columns: Iterable<String>) {
    this.databaseClient.sql(
        "CREATE UNIQUE INDEX index_${tableName}_${columns.joinToString("_")} " +
            "ON $tableName (${columns.joinToString(",")})"
    )
        .fetch()
        .rowsUpdated()
        .awaitSingle()
}

suspend fun R2dbcEntityTemplate.dropIndex(tableName: String, columns: Iterable<String>) {
    this.databaseClient.sql(
        "DROP INDEX index_${tableName}_${columns.joinToString("_")} " +
            "ON $tableName"
    )
        .fetch()
        .rowsUpdated()
        .awaitSingle()
}

suspend fun R2dbcEntityTemplate.dropTable(tableName: String) {
    this.databaseClient.sql(
        "DROP TABLE $tableName"
    )
        .fetch()
        .rowsUpdated()
        .awaitSingle()
}
