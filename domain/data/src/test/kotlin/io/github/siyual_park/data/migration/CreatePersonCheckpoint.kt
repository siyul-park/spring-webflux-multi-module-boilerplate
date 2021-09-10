package io.github.siyual_park.data.migration

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreatePersonCheckpoint: Migration {
    private val tableName = "persons"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                    "(" +
                    "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR ," +
                    "age INT ," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
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
}