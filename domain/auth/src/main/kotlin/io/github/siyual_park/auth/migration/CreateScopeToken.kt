package io.github.siyual_park.auth.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createIndex
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.dropTable
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreateScopeToken : Migration {
    private val tableName = "scope_tokens"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +

                "name VARCHAR(50) NOT NULL," +
                "description VARCHAR(150)," +
                "system BOOLEAN NOT NULL," +
                "default BOOLEAN NOT NULL," +

                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        entityTemplate.createUniqueIndex(tableName, listOf("name"))
        entityTemplate.createIndex(tableName, listOf("system"))
        entityTemplate.createIndex(tableName, listOf("default"))
    }
    override suspend fun down(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.dropTable(tableName)
    }
}
