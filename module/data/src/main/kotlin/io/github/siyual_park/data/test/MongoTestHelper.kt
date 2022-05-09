package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import de.flapdoodle.embed.mongo.MongodExecutable
import io.github.siyual_park.data.converter.BinaryToULIDConverter
import io.github.siyual_park.data.converter.ULIDToBinaryConverter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.UUID

class MongoTestHelper : ResourceTestHelper {
    lateinit var mongodExecutable: MongodExecutable
    lateinit var mongoClient: MongoClient
    lateinit var mongoTemplate: ReactiveMongoTemplate

    private val database = UUID.randomUUID().toString()

    @BeforeAll
    override fun setUp() {
        val mongodExecutableAndClient = createEmbeddedMongoDBClients()
        mongodExecutable = mongodExecutableAndClient.first
        mongodExecutable.start()

        mongoClient = mongodExecutableAndClient.second
        mongoTemplate = createReactiveMongoTemplate(
            mongoClient,
            database,
            mutableListOf<Converter<*, *>>(
                BinaryToULIDConverter(),
                ULIDToBinaryConverter(),
            )
        )
    }

    @AfterAll
    override fun tearDown() {
        mongodExecutable.stop()
    }
}
