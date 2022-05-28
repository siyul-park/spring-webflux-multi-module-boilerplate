package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.dummy.DummyCreateClientPayload
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ClientCredentialsGrantAuthenticateStrategyTest : ClientTestHelper() {
    private val authorizationStrategy = ClientCredentialsGrantAuthenticateStrategy(clientStorage)

    @Test
    fun authenticate() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val principal = authorizationStrategy.authenticate(
            ClientCredentialsGrantPayload(
                client.id, client.getCredential().raw().secret
            )
        )
        assertNotNull(principal)
    }
}
