package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.dummy.DummyCreateScopeTokenPayload
import io.github.siyual_park.auth.dummy.DummyScopeNameFactory
import io.github.siyual_park.auth.dummy.DummyStringFactory
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.event.EventEmitter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException

class ScopeTokenFactoryTest : DataTestHelper() {
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

    @Test
    fun upsert() = blocking {
        val name = DummyScopeNameFactory.create(10)
        val description = DummyStringFactory.create(10)

        val scopeToken1 = scopeTokenFactory.upsert(name)
        assertEquals(name, scopeToken1.name)

        val scopeToken2 = scopeTokenFactory.upsert(name)
        assertEquals(name, scopeToken2.name)
        assertEquals(scopeToken1.id, scopeToken2.id)

        val scopeToken3 = scopeTokenFactory.upsert(CreateScopeTokenPayload(name, description))
        assertEquals(name, scopeToken3.name)
        assertEquals(description, scopeToken3.description)
        assertEquals(scopeToken1.id, scopeToken3.id)
    }

    @Test
    fun create() = blocking {
        val payload = DummyCreateScopeTokenPayload.create()

        val scopeToken = scopeTokenFactory.create(payload)
        assertEquals(payload.name, scopeToken.name)
        assertEquals(payload.description, scopeToken.description)

        assertThrows<DataIntegrityViolationException> { scopeTokenFactory.create(payload) }
    }
}
