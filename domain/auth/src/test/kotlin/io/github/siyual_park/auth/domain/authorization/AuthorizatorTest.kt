package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
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

class AuthorizatorTest : DataTestHelper() {
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

    init {
        authorizator.register(AllowAllAuthorizeFilter(), PrincipalHasScopeAuthorizeStrategy())
    }

    @Test
    fun authorize() = blocking {
        val scopeToken1 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }
        val scopeToken2 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }

        val principal = object : Principal {
            override val id = ULID.randomULID()
            override var scope = setOf(scopeToken1)
        }

        assertTrue(authorizator.authorize(principal, listOf(scopeToken1)))
        assertFalse(authorizator.authorize(principal, listOf(scopeToken2)))
        assertTrue(authorizator.authorize(principal, scopeToken1))
        assertFalse(authorizator.authorize(principal, scopeToken2))
        assertTrue(authorizator.authorize(principal, listOf(scopeToken1, scopeToken2)))
        assertFalse(authorizator.authorize(principal, listOf(listOf(scopeToken1, scopeToken2))))
        assertTrue(authorizator.authorize(principal, listOf(listOf(scopeToken1), listOf(scopeToken2))))
    }
}
