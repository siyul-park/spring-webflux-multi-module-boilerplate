package io.github.siyual_park.user.migration

import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.dropTable
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

class CreateUserCredential : Migration {
    private val tableName = "user_credentials"

    override suspend fun up(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.databaseClient.sql(
            "CREATE TABLE $tableName" +
                "(" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +

                "user_id BIGINT NOT NULL REFERENCES users (id)," +

                "password VARCHAR NOT NULL," +
                "hash_algorithm VARCHAR(50) NOT NULL," +

                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")"
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        entityTemplate.createUniqueIndex(tableName, listOf("user_id"))
    }
    override suspend fun down(entityTemplate: R2dbcEntityTemplate) {
        entityTemplate.dropTable(tableName)
    }
}
