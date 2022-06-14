package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.domain.authorization.AllowAllAuthorizeFilter
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.PrincipalHasScopeAuthorizeStrategy
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.event.EventEmitter
import io.github.siyual_park.ulid.ULID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class ScopeEvaluatorTest : DataTestHelper() {
    internal class TestPrincipal(
        override val id: ULID,
        override var scope: Set<ScopeToken>
    ) : Principal

    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
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
    private val authorizator = Authorizator(scopeTokenStorage)

    private val scopeEvaluator = ScopeEvaluator(scopeTokenStorage, authorizator)

    init {
        authorizator.register(AllowAllAuthorizeFilter(), PrincipalHasScopeAuthorizeStrategy())
    }

    @Test
    fun hasPermission() = blocking {
        val scopeToken1 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }
        val scopeToken2 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }

        val principal = TestPrincipal(
            ULID.randomULID(),
            setOf(scopeToken1)
        )

        val athentication = UsernamePasswordAuthenticationToken(principal, "")

        assertTrue(scopeEvaluator.hasPermission(athentication, null, scopeToken1.name))
        assertTrue(scopeEvaluator.hasPermission(athentication, null, listOf(scopeToken1.name)))
        assertFalse(scopeEvaluator.hasPermission(athentication, null, scopeToken2.name))
        assertFalse(scopeEvaluator.hasPermission(athentication, null, listOf(scopeToken2.name)))
        assertTrue(scopeEvaluator.hasPermission(athentication, null, listOf(scopeToken1.name, scopeToken2.name)))
        assertFalse(scopeEvaluator.hasPermission(athentication, null, listOf(listOf(scopeToken1.name, scopeToken2.name))))
        assertTrue(scopeEvaluator.hasPermission(athentication, listOf(null), listOf(scopeToken1.name)))
        assertFalse(scopeEvaluator.hasPermission(athentication, listOf(null), listOf(scopeToken1.name, scopeToken2.name)))
        assertTrue(scopeEvaluator.hasPermission(athentication, listOf(null, null), listOf(scopeToken1.name, scopeToken2.name)))
        assertTrue(scopeEvaluator.hasPermission(athentication, listOf(listOf(null)), listOf(listOf(scopeToken1.name))))
    }
}
