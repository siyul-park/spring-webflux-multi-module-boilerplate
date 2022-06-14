package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.persistence.loadOrFail
import io.mockk.coVerify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Optional

class ClientTest : ClientTestHelper() {
    @Test
    fun sync() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        val other = MockCreateClientPayloadFactory.create()

        client.name = other.name
        client.origin = other.origin

        assertEquals(other.name, client.name)
        assertEquals(other.origin, client.origin)

        client.sync()

        assertEquals(other.name, client.name)
        assertEquals(other.origin, client.origin)

        val exist = clientStorage.loadOrFail(client.id)

        assertEquals(other.name, exist.name)
        assertEquals(other.origin, exist.origin)
    }

    @Test
    fun grant() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        val client1 = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val client2 = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        val clients = clientStorage.load(listOf(client1.id, client2.id)).toList()

        val loaded1 = clients.find { it.id == client1.id }
        val loaded2 = clients.find { it.id == client2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        loaded1?.grant(customScope)
        assertTrue(loaded1?.has(customScope) == true)
        loaded1?.getScope(deep = false)?.toSet()?.also {
            assertTrue(it.contains(customScope))
        }

        coVerify(exactly = 1) { clientScopeRepository.findAll(any()) }

        loaded2?.grant(customScope)
        assertTrue(loaded2?.has(customScope) == true)
        loaded2?.getScope(deep = false)?.toSet()?.also {
            assertTrue(it.contains(customScope))
        }

        coVerify(exactly = 2) { clientScopeRepository.findAll(any()) }
    }

    @Test
    fun revoke() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        val template = MockCreateClientPayloadFactory.Template(
            scope = Optional.of(listOf(customScope))
        )

        val client1 = MockCreateClientPayloadFactory.create(template)
            .let { clientFactory.create(it) }
        val client2 = MockCreateClientPayloadFactory.create(template)
            .let { clientFactory.create(it) }

        val clients = clientStorage.load(listOf(client1.id, client2.id)).toList()

        val loaded1 = clients.find { it.id == client1.id }
        val loaded2 = clients.find { it.id == client2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        loaded1?.revoke(customScope)
        assertTrue(loaded1?.has(customScope) == false)
        loaded1?.getScope(deep = false)?.toSet()?.also {
            assertFalse(it.contains(customScope))
        }

        coVerify(exactly = 1) { clientScopeRepository.findAll(any()) }

        loaded2?.revoke(customScope)
        assertTrue(loaded2?.has(customScope) == false)
        loaded2?.getScope(deep = false)?.toSet()?.also {
            assertFalse(it.contains(customScope))
        }

        coVerify(exactly = 2) { clientScopeRepository.findAll(any()) }
    }

    @Test
    fun getScope() = blocking {
        val customScope = scopeTokenFactory.upsert(MockScopeNameFactory.create())
        val template = MockCreateClientPayloadFactory.Template(
            scope = Optional.of(listOf(customScope))
        )

        val client1 = MockCreateClientPayloadFactory.create(template)
            .let { clientFactory.create(it) }
        val client2 = MockCreateClientPayloadFactory.create(template)
            .let { clientFactory.create(it) }

        val clients = clientStorage.load(listOf(client1.id, client2.id)).toList()

        val loaded1 = clients.find { it.id == client1.id }
        val loaded2 = clients.find { it.id == client2.id }

        assertNotNull(loaded1)
        assertNotNull(loaded2)

        assertEquals(loaded1?.getScope(deep = false)?.toSet(), client1.getScope(deep = false).toSet())
        assertEquals(loaded2?.getScope(deep = false)?.toSet(), client2.getScope(deep = false).toSet())

        coVerify(exactly = 3) { clientScopeRepository.findAll(any()) }

        assertEquals(loaded1?.getScope(deep = true)?.toSet(), client1.getScope(deep = true).toSet())
        assertEquals(loaded2?.getScope(deep = true)?.toSet(), client2.getScope(deep = true).toSet())

        coVerify(exactly = 6) { clientScopeRepository.findAll(any()) }

        loaded1?.getScope(deep = false)?.toSet()
        loaded1?.getScope(deep = false)?.toSet()

        coVerify(exactly = 8) { clientScopeRepository.findAll(any()) }

        loaded2?.getScope(deep = false)?.toSet()
        loaded2?.getScope(deep = false)?.toSet()

        coVerify(exactly = 9) { clientScopeRepository.findAll(any()) }
    }

    @Test
    fun clear() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        client.clear()
        Assertions.assertNull(clientStorage.load(client.id))
    }

    @Test
    fun toPrincipal() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val principal = client.toPrincipal()

        assertEquals(client.id, principal.id)
        assertEquals(client.id, principal.clientId)
        assertEquals(client.getScope(deep = true).toSet(), principal.scope)
    }
}
