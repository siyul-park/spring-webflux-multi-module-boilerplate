package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationDataRepository
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import io.github.siyual_park.data.test.DataTestHelper
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

    private val scopeRelationDataRepository = ScopeRelationDataRepository(entityOperations)
    private val scopeTokenDataRepository = ScopeTokenDataRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenDataRepository, scopeRelationDataRepository)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenDataRepository, scopeTokenMapper)

    private val principalHasScopeAuthorizeStrategy = PrincipalHasScopeAuthorizeStrategy()

    @Test
    fun authorize() = blocking {
        val scopeToken = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }

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
