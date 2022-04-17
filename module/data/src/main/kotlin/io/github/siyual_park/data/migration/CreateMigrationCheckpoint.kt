package io.github.siyual_park.data.migration

import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateMigrationCheckpoint(
    private val entityOperations: R2dbcEntityOperations
) : Migration {
    private val tableName = "migration_checkpoints"

    override suspend fun up() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BYTEA PRIMARY KEY, " +

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

    override suspend fun down() {
        entityOperations.dropTable(tableName)
    }

    suspend fun isApplied(): Boolean {
        return entityOperations.isExistTable(tableName)
    }
}
