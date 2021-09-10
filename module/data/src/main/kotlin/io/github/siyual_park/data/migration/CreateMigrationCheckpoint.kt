package io.github.siyual_park.data.migration

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreateMigrationCheckpoint : Migration {
    private val tableName = "migration_checkpoints"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "version BIGINT," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
    override suspend fun down(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "DROP TABLE $tableName"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    suspend fun isApplied(entityTemplate: R2dbcEntityTemplate): Boolean {
        val result = entityTemplate.databaseClient.sql(
            "SELECT * " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE UPPER(TABLE_NAME) = '${tableName.uppercase()}' "
        )
            .fetch()
            .all()
            .asFlow()
            .toList()

        return result.isNotEmpty()
    }
}
