package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.MockCreateScopeTokenPayloadFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.data.test.DataTestHelper
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

    private val scopeRelationEntityRepository = ScopeRelationEntityRepository(entityOperations)
    private val scopeTokenEntityRepository = ScopeTokenEntityRepository(entityOperations)

    private val scopeTokenMapper = ScopeTokenMapper(scopeTokenEntityRepository, scopeRelationEntityRepository)
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenEntityRepository, scopeTokenMapper)

    private val authorizator = Authorizator(scopeTokenStorage)

    init {
        authorizator.register(AllowAllAuthorizeFilter(), PrincipalHasScopeAuthorizeStrategy())
    }

    @Test
    fun authorize() = blocking {
        val scopeToken1 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }
        val scopeToken2 = MockCreateScopeTokenPayloadFactory.create()
            .let { scopeTokenStorage.save(it) }

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
