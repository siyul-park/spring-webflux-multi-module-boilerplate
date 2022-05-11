package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.dummy.DummyCreateScopeTokenPayload
import io.github.siyual_park.auth.dummy.DummyStringFactory
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.event.EventEmitter
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration

class TokenTest : DataTestHelper() {
    internal class TestPrincipal(
        override val id: String,
        override var scope: Set<ScopeToken>
    ) : Principal

    internal class TestClaimEmbeddingStrategy : ClaimEmbeddingStrategy<TestPrincipal> {
        override suspend fun embedding(principal: TestPrincipal): Map<String, Any> {
            return mapOf(
                "tid" to principal.id
            )
        }
    }

    private val eventEmitter = EventEmitter()

    private val scopeRelationRepository = ScopeRelationRepository(entityOperations)
    private val scopeTokenRepository = ScopeTokenRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(
        scopeTokenRepository,
        scopeRelationRepository,
        transactionalOperator,
        eventEmitter
    )

    private val scopeTokenFactory = ScopeTokenFactory(scopeTokenRepository, scopeTokenMapper, eventEmitter)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)
    private val claimEmbedder = ClaimEmbedder()
    private val tokenRepository = TokenRepository(mongoTemplate, eventEmitter)
    private val tokenMapper = TokenMapper(tokenRepository, scopeTokenStorage, eventEmitter)

    private val tokenFactoryProvider = TokenFactoryProvider(claimEmbedder, tokenRepository, tokenMapper)

    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))

        claimEmbedder.register(TestPrincipal::class, TestClaimEmbeddingStrategy())
    }

    @Test
    fun create() = blocking {
        val scopeToken = DummyCreateScopeTokenPayload.create()
            .let { scopeTokenFactory.create(it) }
        val principal = TestPrincipal(DummyStringFactory.create(10), setOf(scopeToken))

        val template = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenFactoryProvider.get(template)

        val token = factory.create(principal, Duration.ofMinutes(10))

        assertEquals("test", token.type)
        assertEquals(principal.id, token["tid"])
        assertTrue(token.has(scopeToken))
        assertTrue(token.isActivated())
    }

    @Test
    fun setGet() = blocking {
        val principal = TestPrincipal(DummyStringFactory.create(10), setOf())

        val template = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenFactoryProvider.get(template)

        val token = factory.create(principal, Duration.ofMinutes(10))

        val key = DummyStringFactory.create(10)
        val value = DummyStringFactory.create(10)

        token[key] = value
        assertEquals(value, token[key])
    }

    @Test
    fun getScope() = blocking {
        val scopeToken = DummyCreateScopeTokenPayload.create()
            .let { scopeTokenFactory.create(it) }
        val principal = TestPrincipal(DummyStringFactory.create(10), setOf(scopeToken))

        val template = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenFactoryProvider.get(template)

        val token = factory.create(principal, Duration.ofMinutes(10))

        val scope1 = token.getScope().toList()
        assertEquals(1, scope1.size)
        assertTrue(scope1.contains(scopeToken))

        val scope2 = token.getScope(deep = true).toList()
        assertEquals(1, scope2.size)
        assertTrue(scope2.contains(scopeToken))
    }

    @Test
    fun grant() = blocking {
        val scopeToken = DummyCreateScopeTokenPayload.create()
            .let { scopeTokenFactory.create(it) }
        val principal = TestPrincipal(DummyStringFactory.create(10), setOf())

        val template = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenFactoryProvider.get(template)

        val token = factory.create(principal, Duration.ofMinutes(10))

        assertFalse(token.has(scopeToken))

        token.grant(scopeToken)

        assertTrue(token.has(scopeToken))
    }

    @Test
    fun revoke() = blocking {
        val scopeToken = DummyCreateScopeTokenPayload.create()
            .let { scopeTokenFactory.create(it) }
        val principal = TestPrincipal(DummyStringFactory.create(10), setOf(scopeToken))

        val template = TokenTemplate(
            type = "test",
            limit = listOf(
                "tid" to 1
            )
        )
        val factory = tokenFactoryProvider.get(template)

        val token = factory.create(principal, Duration.ofMinutes(10))

        assertTrue(token.has(scopeToken))

        token.revoke(scopeToken)

        assertFalse(token.has(scopeToken))
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
