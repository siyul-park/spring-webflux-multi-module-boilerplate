package io.github.siyual_park.data.repository.mongo

import com.google.common.cache.CacheBuilder
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.data.repository.QueryRepositoryTestHelper
import io.github.siyual_park.data.repository.mongo.migration.CreatePerson
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration

class InMemoryMongoCacheRepositoryTest : QueryRepositoryTestHelper(
    repositories = {
        listOf(
            MongoRepositoryBuilder<Person, ULID>(mongoTemplate, Person::class)
                .enableCache {
                    CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(Duration.ofMinutes(2))
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .maximumSize(1_000)
                }
                .build()
        )
    }
) {
    init {
        migrationManager.register(CreatePerson(mongoTemplate))
    }

    companion object {
        private val helper = MongoTestHelper()

        val mongoTemplate: ReactiveMongoTemplate
            get() = helper.mongoTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
