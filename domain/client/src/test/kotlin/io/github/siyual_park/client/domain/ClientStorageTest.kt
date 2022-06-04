package io.github.siyual_park.client.domain

import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClientStorageTest : ClientTestHelper() {
    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("confidential(client):pack")
            scopeTokenFactory.upsert("public(client):pack")
        }
    }

    @Test
    fun load() = blocking {
        val client1 = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val client2 = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        assertEquals(client1, clientStorage.load(client1.id))
        assertEquals(client2, clientStorage.load(client2.id))
        assertEquals(client1, clientStorage.load(client1.name))
        assertEquals(client2, clientStorage.load(client2.name))

        assertEquals(setOf(client1, client2), clientStorage.load(listOf(client1.id, client2.id)).toSet())
    }
}
