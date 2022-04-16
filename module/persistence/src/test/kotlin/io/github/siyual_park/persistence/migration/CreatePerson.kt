package io.github.siyual_park.persistence.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isExistTable
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreatePerson : Migration {
    private val tableName = "persons"

    override suspend fun up(entityOperations: R2dbcEntityOperations) {
        if (entityOperations.isExistTable(tableName)) {
            return
        }
        entityOperations.fetchSQL(
            "CREATE TABLE $tableName" +
                "(" +
                "id BINARY(16) NOT NULL PRIMARY KEY, " +

                "name VARCHAR(64) NOT NULL, " +
                "age INT NOT NULL, " +

                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )

        entityOperations.createUniqueIndex(tableName, listOf("name"))
    }

    override suspend fun down(entityOperations: R2dbcEntityOperations) {
        if (!entityOperations.isExistTable(tableName)) {
            return
        }
        entityOperations.dropTable(tableName)
    }
}
