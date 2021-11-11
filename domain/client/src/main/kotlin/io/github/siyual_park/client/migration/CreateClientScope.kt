package io.github.siyual_park.client.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createIndex
import io.github.siyual_park.data.migration.createUpdatedAtTrigger
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isDriver
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateClientScope : Migration {
    private val tableName = "client_scopes"

    override suspend fun up(entityOperations: R2dbcEntityOperations) {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id SERIAL PRIMARY KEY, " +

                    "client_id INTEGER NOT NULL REFERENCES clients (id), " +
                    "scope_token_id INTEGER NOT NULL REFERENCES scope_tokens (id), " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            )
            entityOperations.createUpdatedAtTrigger(tableName)
        } else {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +

                    "client_id BIGINT NOT NULL REFERENCES clients (id), " +
                    "scope_token_id BIGINT NOT NULL REFERENCES scope_tokens (id), " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
            )
        }

        entityOperations.createIndex(tableName, listOf("client_id"))
        entityOperations.createIndex(tableName, listOf("scope_token_id"))
    }

    override suspend fun down(entityOperations: R2dbcEntityOperations) {
        entityOperations.dropTable(tableName)
    }
}
