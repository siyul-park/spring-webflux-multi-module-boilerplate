package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration

class TokenFactoryTest : DataTestHelper() {
    internal class TestPrincipal(
        override val id: ULID,
        override var scope: Set<ScopeToken>
    ) : Principal

    internal class TestClaimEmbeddingStrategy : ClaimEmbeddingStrategy<TestPrincipal> {
        override val clazz = TestPrincipal::class
        override suspend fun embedding(principal: TestPrincipal): Map<String, Any> {
            return mapOf(
                "tid" to principal.id
            )
        }
    }

    private val scopeRelationEntityRepository = ScopeRelationEntityRepository(entityOperations)
    private val scopeTokenEntityRepository = ScopeTokenEntityRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenEntityRepository, scopeRelationEntityRepository)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenEntityRepository, scopeTokenMapper)
    private val claimEmbedder = ClaimEmbedder()
    private val tokenEntityRepository = TokenEntityRepository(mongoTemplate)
    private val tokenMapper = TokenMapper(tokenEntityRepository, scopeTokenStorage)

    private val tokenStorage = TokenStorage(claimEmbedder, tokenEntityRepository, tokenMapper)

    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))

        claimEmbedder.register(TypeMatchClaimFilter(TestPrincipal::class), TestClaimEmbeddingStrategy())
    }

    @Test
    fun create() = blocking {
        val scopeToken = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }
        val principal = TestPrincipal(ULID.randomULID(), setOf(scopeToken))

        val template = TokenTemplate(
            type = "test",
            age = Duration.ofMinutes(10),
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenStorage.createFactory(template)

        val token1 = factory.create(principal)

        assertEquals("test", token1.type)
        assertEquals(principal.id, token1["tid"]?.let { ULID.fromString(it.toString()) })
        assertTrue(token1.has(scopeToken))

        val token2 = factory.create(principal)

        assertEquals("test", token2.type)
        assertEquals(principal.id, token1["tid"]?.let { ULID.fromString(it.toString()) })
        assertTrue(token2.has(scopeToken))
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
