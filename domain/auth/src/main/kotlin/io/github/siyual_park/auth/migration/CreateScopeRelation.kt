package io.github.siyual_park.auth.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createIndex
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.createUpdatedAtTrigger
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isDriver
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateScopeRelation(
    private val entityOperations: R2dbcEntityOperations
) : Migration {
    private val tableName = "scope_relations"

    override suspend fun up() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id SERIAL PRIMARY KEY, " +

                    "parent_id BYTEA NOT NULL REFERENCES scope_tokens (id), " +
                    "child_id BYTEA NOT NULL REFERENCES scope_tokens (id), " +

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

                    "parent_id BINARY(16) NOT NULL REFERENCES scope_tokens (id), " +
                    "child_id BINARY(16) NOT NULL REFERENCES scope_tokens (id), " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
            )
        }

        entityOperations.createUniqueIndex(tableName, listOf("parent_id", "child_id"))
        entityOperations.createIndex(tableName, listOf("parent_id"))
        entityOperations.createIndex(tableName, listOf("child_id"))
    }

    override suspend fun down() {
        entityOperations.dropTable(tableName)
    }
}
