package io.github.siyual_park.user.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.createUpdatedAtTrigger
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isDriver
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateUserCredential(
    private val entityOperations: R2dbcEntityOperations
) : Migration {
    private val tableName = "user_credentials"

    override suspend fun up() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id SERIAL PRIMARY KEY, " +

                    "user_id BYTEA NOT NULL REFERENCES users (id), " +

                    "password VARCHAR(128) NOT NULL, " +
                    "hash_algorithm VARCHAR(64) NOT NULL, " +

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

                    "user_id BINARY(16) NOT NULL REFERENCES users (id), " +

                    "password VARCHAR(128) NOT NULL, " +
                    "hash_algorithm VARCHAR(64) NOT NULL, " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")"
            )
        }

        entityOperations.createUniqueIndex(tableName, listOf("user_id"))
    }

    override suspend fun down() {
        entityOperations.dropTable(tableName)
    }
}
