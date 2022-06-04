package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientEntityClaimEmbeddingStrategyTest : ClientTestHelper() {
    private val clientEntityClaimEmbeddingStrategy = ClientEntityClaimEmbeddingStrategy()

    @Test
    fun embedding() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val principal = client.toPrincipal()

        val claim = clientEntityClaimEmbeddingStrategy.embedding(principal)

        assertEquals(client.id, claim["cid"])
    }
}
