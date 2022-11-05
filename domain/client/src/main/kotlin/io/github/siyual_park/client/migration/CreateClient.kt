package io.github.siyual_park.client.migration

import com.mongodb.BasicDBObject
import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createUniqueIndex
import io.github.siyual_park.data.migration.createUpdatedAtTrigger
import io.github.siyual_park.data.migration.dropTable
import io.github.siyual_park.data.migration.fetchSQL
import io.github.siyual_park.data.migration.isDriver
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.r2dbc.core.R2dbcEntityOperations

class CreateClient(
    private val entityOperations: R2dbcEntityOperations,
    private val mongoTemplate: ReactiveMongoTemplate,
) : Migration {
    private val tableName = "clients"

    override suspend fun up() {
        if (entityOperations.isDriver("PostgreSQL")) {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BYTEA PRIMARY KEY, " +

                    "name VARCHAR(64) NOT NULL, " +
                    "origins VARCHAR(2048)[] NOT NULL, " +

                    "secret VARCHAR(32), " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    ")"
            )
            entityOperations.createUpdatedAtTrigger(tableName)
        } else {
            entityOperations.fetchSQL(
                "CREATE TABLE $tableName" +
                    "(" +
                    "id BINARY(16) NOT NULL PRIMARY KEY, " +

                    "name VARCHAR(64) NOT NULL, " +
                    "origins VARCHAR(2048) ARRAY NOT NULL, " +

                    "secret VARCHAR(32), " +

                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    ")"
            )
        }

        entityOperations.createUniqueIndex(tableName, listOf("name"))

        mongoTemplate.getCollection("tokens").awaitSingle().apply {
            createIndex(BasicDBObject("claims.cid", 1)).awaitFirstOrNull()
        }
    }

    override suspend fun down() {
        entityOperations.dropTable(tableName)

        mongoTemplate.getCollection("tokens").awaitSingle().apply {
            dropIndex(BasicDBObject("claims.cid", 1)).awaitFirstOrNull()
        }
    }
}
