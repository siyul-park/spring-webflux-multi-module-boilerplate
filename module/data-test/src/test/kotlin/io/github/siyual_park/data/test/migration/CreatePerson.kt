package io.github.siyual_park.data.test.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.dropTable
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreatePerson : Migration {
    private val tableName = "persons"

    override suspend fun up(entityOperations: R2dbcEntityOperations) {
        entityOperations.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`name` VARCHAR(64) NOT NULL, " +
                "`age` INT NOT NULL, " +
                "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun down(entityOperations: R2dbcEntityOperations) {
        entityOperations.dropTable(tableName)
    }
}
