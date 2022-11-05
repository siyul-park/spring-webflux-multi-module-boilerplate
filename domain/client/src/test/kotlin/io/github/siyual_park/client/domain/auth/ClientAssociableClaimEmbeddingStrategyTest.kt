package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.client.domain.ClientTestHelper
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientAssociableClaimEmbeddingStrategyTest : ClientTestHelper() {
    private val clientAssociableClaimEmbeddingStrategy = ClientAssociableClaimEmbeddingStrategy()

    @Test
    fun embedding() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val principal = client.toPrincipal()

        val claim = clientAssociableClaimEmbeddingStrategy.embedding(principal)

        assertEquals(client.id, claim["cid"])
    }
}
