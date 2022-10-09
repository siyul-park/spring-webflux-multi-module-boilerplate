package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class ClientStorageTest : ClientTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenStorage.upsert("confidential(client):pack")
            scopeTokenStorage.upsert("public(client):pack")
        }
    }

    @Test
    fun `save, when use confidential`() = blocking {
        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                type = Optional.of(ClientType.CONFIDENTIAL)
            )
        )
        val client = clientStorage.save(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)
        assertEquals(client.id, client.getCredential().clientId)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("confidential(client):pack"), scope[0])
    }

    @Test
    fun `save, when use public`() = blocking {
        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                type = Optional.of(ClientType.PUBLIC)
            )
        )
        val client = clientStorage.save(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("public(client):pack"), scope[0])
    }

    @Test
    fun `save, when use custom scope`() = blocking {
        val customScope = scopeTokenStorage.upsert(MockScopeNameFactory.create())

        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                scope = Optional.of(listOf(customScope))
            )
        )
        val client = clientStorage.save(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(customScope, scope[0])
    }

    @Test
    fun load() = blocking {
        val client1 = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val client2 = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }

        assertEquals(client1, this@ClientStorageTest.clientStorage.load(client1.id))
        assertEquals(client2, this@ClientStorageTest.clientStorage.load(client2.id))
        assertEquals(client1, this@ClientStorageTest.clientStorage.load(client1.name))
        assertEquals(client2, this@ClientStorageTest.clientStorage.load(client2.name))

        assertEquals(setOf(client1, client2), this@ClientStorageTest.clientStorage.load(listOf(client1.id, client2.id)).toSet())
    }
}
