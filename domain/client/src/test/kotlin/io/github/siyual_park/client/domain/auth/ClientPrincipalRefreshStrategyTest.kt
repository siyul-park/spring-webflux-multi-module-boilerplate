package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClientPrincipalRefreshStrategyTest : ClientTestHelper() {
    private val clientPrincipalRefreshStrategy = ClientPrincipalRefreshStrategy(clientStorage)

    @Test
    fun refresh() = blocking {
        val clientScope = scopeTokenFactory.upsert("confidential(client):pack")
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        clientScope.grant(customScope)

        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val principal = ClientPrincipal(clientId = client.id, scope = setOf())

        val refreshed = clientPrincipalRefreshStrategy.refresh(principal)

        assertEquals(refreshed.id, principal.id)
        assertEquals(refreshed.clientId, principal.clientId)
        assertTrue(refreshed.scope.contains(customScope))
    }
}
