package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.event.EventEmitter
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import java.util.Optional

class ScopeTokenTest : DataTestHelper() {
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
    fun create() = blocking {
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken = scopeTokenFactory.create(payload)

        assertEquals(payload.name, scopeToken.name)
        assertEquals(payload.description, scopeToken.description)
        assertTrue(scopeToken.isSystem())
        assertFalse(scopeToken.isPacked())
    }

    @Test
    fun has() = blocking {
        val packPayload = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken1 = scopeTokenFactory.create(packPayload)
        val scopeToken2 = scopeTokenFactory.create(payload)

        assertTrue(scopeToken1.isPacked())
        assertFalse(scopeToken2.isPacked())
        assertFalse(scopeToken1.has(scopeToken2))

        scopeToken1.grant(scopeToken2)

        assertTrue(scopeToken1.has(scopeToken2))
    }

    @Test
    fun grant() = blocking {
        val packPayload = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken1 = scopeTokenFactory.create(packPayload)
        val scopeToken2 = scopeTokenFactory.create(payload)

        scopeToken1.grant(scopeToken2)
        assertTrue(scopeToken1.has(scopeToken2))

        assertThrows<UnsupportedOperationException> { scopeToken2.grant(scopeToken1) }
        assertThrows<DataIntegrityViolationException> { scopeToken1.grant(scopeToken2) }
    }

    @Test
    fun revoke() = blocking {
        val packPayload = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken1 = scopeTokenFactory.create(packPayload)
        val scopeToken2 = scopeTokenFactory.create(payload)

        scopeToken1.grant(scopeToken2)
        assertTrue(scopeToken1.has(scopeToken2))

        scopeToken1.revoke(scopeToken2)
        assertFalse(scopeToken1.has(scopeToken2))

        assertThrows<UnsupportedOperationException> { scopeToken2.revoke(scopeToken1) }
        assertThrows<EmptyResultDataAccessException> { scopeToken1.revoke(scopeToken2) }
    }

    @Test
    fun children() = blocking {
        val packPayload = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val payload = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken1 = scopeTokenFactory.create(packPayload)
        val scopeToken2 = scopeTokenFactory.create(payload)

        scopeToken1.grant(scopeToken2)

        val children = scopeToken1.children().toList()
        assertEquals(1, children.size)
        assertEquals(scopeToken2, children[0])

        assertThrows<UnsupportedOperationException> { scopeToken2.children().toList() }
    }

    @Test
    fun resolve() = blocking {
        val packPayload1 = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val packPayload2 = MockCreateScopeTokenPayloadFactory.create(
            MockCreateScopeTokenPayloadFactory.Template(
                name = Optional.of(MockScopeNameFactory.create(10, "pack"))
            )
        )
        val payload1 = MockCreateScopeTokenPayloadFactory.create()
        val payload2 = MockCreateScopeTokenPayloadFactory.create()

        val scopeToken1 = scopeTokenFactory.create(packPayload1)
        val scopeToken2 = scopeTokenFactory.create(packPayload2)
        val scopeToken3 = scopeTokenFactory.create(payload1)
        val scopeToken4 = scopeTokenFactory.create(payload2)

        scopeToken1.grant(scopeToken2)
        scopeToken1.grant(scopeToken3)
        scopeToken2.grant(scopeToken4)

        val children1 = scopeToken1.resolve().toList()
        assertEquals(2, children1.size)
        assertTrue(children1.contains(scopeToken3))
        assertTrue(children1.contains(scopeToken4))

        val children2 = scopeToken2.resolve().toList()
        assertEquals(1, children2.size)
        assertTrue(children2.contains(scopeToken4))

        val children3 = scopeToken3.resolve().toList()
        assertEquals(1, children3.size)
        assertTrue(children3.contains(scopeToken3))

        val children4 = scopeToken4.resolve().toList()
        assertEquals(1, children4.size)
        assertTrue(children4.contains(scopeToken4))
    }
}
