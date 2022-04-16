package io.github.siyual_park.data.migration

import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateMigrationCheckpoint : Migration {
    private val tableName = "migration_checkpoints"

    override suspend fun up(entityOperations: R2dbcEntityOperations) {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id SERIAL PRIMARY KEY, " +

                    "version BIGINT NOT NULL, " +
                    "status VARCHAR(64) NOT NULL, " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            )
            entityOperations.createUpdatedAtTrigger(tableName)
        } else {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BINARY(16) NOT NULL PRIMARY KEY, " +

                    "version BIGINT NOT NULL, " +
                    "status VARCHAR(64) NOT NULL, " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
            )
        }
    }

    override suspend fun down(entityOperations: R2dbcEntityOperations) {
        entityOperations.dropTable(tableName)
    }

    suspend fun isApplied(entityOperations: R2dbcEntityOperations): Boolean {
        return entityOperations.isExistTable(tableName)
    }
}
