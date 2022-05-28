package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.dummy.DummyCreateClientPayload
import io.github.siyual_park.client.dummy.DummyScopeNameFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClientPrincipalRefreshStrategyTest : ClientTestHelper() {
    private val clientPrincipalRefreshStrategy = ClientPrincipalRefreshStrategy(clientStorage)

    @Test
    fun refresh() = blocking {
        val clientScope = scopeTokenFactory.upsert("confidential(client):pack")
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))
        clientScope.grant(customScope)

        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val principal = ClientPrincipal(id = client.id.toString(), scope = setOf())

        val refreshed = clientPrincipalRefreshStrategy.refresh(principal)

        assertEquals(refreshed.id, principal.id)
        assertEquals(refreshed.clientId, principal.clientId)
        assertTrue(refreshed.scope.contains(customScope))
    }
}
