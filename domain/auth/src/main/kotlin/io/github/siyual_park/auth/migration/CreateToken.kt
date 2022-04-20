package io.github.siyual_park.auth.migration

import com.mongodb.BasicDBObject
import com.mongodb.client.model.IndexOptions
import io.github.siyual_park.data.migration.Migration
import io.github.siyual_park.data.migration.createIndex
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.concurrent.TimeUnit

class CreateToken(
    private val template: ReactiveMongoTemplate
) : Migration {
    private val tableName = "tokens"

    override suspend fun up() {
        template.createCollection(tableName).awaitSingle().apply {
            createIndex(BasicDBObject("type", 1)).awaitSingle()
            createIndex(BasicDBObject("signature", 1), IndexOptions().unique(true)).awaitSingle()
            createIndex(BasicDBObject("expiredAt", 1), IndexOptions().expireAfter(0, TimeUnit.SECONDS)).awaitSingle()

            createIndex(BasicDBObject("claims.pid", 1)).awaitSingle()
        }
    }

    override suspend fun down() {
        template.dropCollection(tableName).awaitSingleOrNull()
    }
}
