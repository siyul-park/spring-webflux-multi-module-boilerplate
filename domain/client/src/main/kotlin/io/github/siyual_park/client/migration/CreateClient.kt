package io.github.siyual_park.client.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUpdatedAtTrigger
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isDriver
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateClient : Migration {
    private val tableName = "clients"

    override suspend fun up(entityOperations: R2dbcEntityOperations) {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id SERIAL PRIMARY KEY, " +

                    "name VARCHAR(64) NOT NULL, " +
                    "type VARCHAR(64) NOT NULL, " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "deleted_at TIMESTAMP" +
                    ")"
            )
            entityOperations.createUpdatedAtTrigger(tableName)
        } else {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +

                    "name VARCHAR(64) NOT NULL, " +
                    "type VARCHAR(64) NOT NULL, " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "deleted_at TIMESTAMP" +
                    ")"
            )
        }
    }

    override suspend fun down(entityOperations: R2dbcEntityOperations) {
        entityOperations.dropTable(tableName)
    }
}
