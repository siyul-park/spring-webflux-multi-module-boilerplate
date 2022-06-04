package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientType
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class ClientFactoryTest : ClientTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("confidential(client):pack")
            scopeTokenFactory.upsert("public(client):pack")
        }
    }

    @Test
    fun `create, when use confidential`() = blocking {
        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                type = Optional.of(ClientType.CONFIDENTIAL)
            )
        )
        val client = clientFactory.create(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)
        assertEquals(client.id, client.getCredential().clientId)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("confidential(client):pack"), scope[0])
    }

    @Test
    fun `create, when use public`() = blocking {
        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                type = Optional.of(ClientType.PUBLIC)
            )
        )
        val client = clientFactory.create(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(scopeTokenStorage.loadOrFail("public(client):pack"), scope[0])
    }

    @Test
    fun `create, when use custom scope`() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        val payload = MockCreateClientPayloadFactory.create(
            MockCreateClientPayloadFactory.Template(
                scope = Optional.of(listOf(customScope))
            )
        )
        val client = clientFactory.create(payload)

        assertEquals(payload.name, client.name)
        assertEquals(payload.origin, client.origin)
        assertEquals(payload.type, client.type)

        val scope = client.getScope(deep = false).toList()
        assertEquals(1, scope.size)
        assertEquals(customScope, scope[0])
    }
}
