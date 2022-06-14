package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
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

class PrincipalHasScopeAuthorizeStrategyTest : DataTestHelper() {
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

    private val principalHasScopeAuthorizeStrategy = PrincipalHasScopeAuthorizeStrategy()

    @Test
    fun authorize() = blocking {
        val scopeToken = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenFactory.create(it) }

        val principal1 = object : Principal {
            override val id = ULID.randomULID()
            override var scope: Set<ScopeToken> = setOf(scopeToken)
        }
        val principal2 = object : Principal {
            override val id = ULID.randomULID()
            override var scope: Set<ScopeToken> = emptySet()
        }

        assertTrue(principalHasScopeAuthorizeStrategy.authorize(principal1, scopeToken))
        assertFalse(principalHasScopeAuthorizeStrategy.authorize(principal2, scopeToken))
    }
}
