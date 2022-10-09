package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ClientCredentialsGrantAuthenticateStrategyTest : ClientTestHelper() {
    private val authorizationStrategy = ClientCredentialsGrantAuthenticateStrategy(this.clientStorage)

    @Test
    fun authenticate() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }

        val principal = authorizationStrategy.authenticate(
            ClientCredentialsGrantPayload(
                client.id, client.getCredential().raw().secret
            )
        )
        assertNotNull(principal)
    }
}
