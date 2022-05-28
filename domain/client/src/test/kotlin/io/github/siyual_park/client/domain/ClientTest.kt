package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.dummy.DummyCreateClientPayload
import io.github.siyual_park.client.dummy.DummyScopeNameFactory
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientCredential
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.converter.StringToURLConverter
import io.github.siyual_park.data.converter.URLToStringConverter
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.github.siyual_park.event.EventEmitter
import io.github.siyual_park.persistence.loadOrFail
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.Optional

class ClientTest : DataTestHelper(
    converters = listOf(
        URLToStringConverter(),
        StringToURLConverter()
    )
) {
    init {
        migrationManager
            .register(CreateScopeToken(entityOperations))
            .register(CreateScopeRelation(entityOperations))
            .register(CreateToken(mongoTemplate))
            .register(CreateClient(entityOperations, mongoTemplate))
            .register(CreateClientCredential(entityOperations))
            .register(CreateClientScope(entityOperations))
    }

    private val eventEmitter = EventEmitter()

    private val scopeRelationRepository = ScopeRelationRepository(entityOperations)
    private val scopeTokenRepository = ScopeTokenRepository(entityOperations)
    private val clientRepository = ClientRepository(entityOperations, eventEmitter)
    private val clientCredentialRepository = ClientCredentialRepository(entityOperations, eventEmitter)
    private val clientScopeRepository = spyk(ClientScopeRepository(entityOperations, eventEmitter))

    private val scopeTokenMapper = ScopeTokenMapper(
        scopeTokenRepository,
        scopeRelationRepository,
        transactionalOperator,
        eventEmitter
    )
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)

    private val scopeTokenFactory = ScopeTokenFactory(scopeTokenRepository, scopeTokenMapper, eventEmitter)

    private val clientMapper = ClientMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)
    private val clientsMapper = ClientsMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)

    private val clientFactory = ClientFactory(clientRepository, clientCredentialRepository, clientMapper, scopeTokenStorage, transactionalOperator, eventEmitter)
    private val clientStorage = ClientStorage(clientRepository, clientMapper, clientsMapper)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("confidential(client):pack")
            scopeTokenFactory.upsert("public(client):pack")
        }
    }

    @Test
    fun sync() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val other = DummyCreateClientPayload.create()

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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))

        val client1 = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val client2 = DummyCreateClientPayload.create()
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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))
        val template = DummyCreateClientPayload.Template(
            scope = Optional.of(listOf(customScope))
        )

        val client1 = DummyCreateClientPayload.create(template)
            .let { clientFactory.create(it) }
        val client2 = DummyCreateClientPayload.create(template)
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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))
        val template = DummyCreateClientPayload.Template(
            scope = Optional.of(listOf(customScope))
        )

        val client1 = DummyCreateClientPayload.create(template)
            .let { clientFactory.create(it) }
        val client2 = DummyCreateClientPayload.create(template)
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
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        client.clear()
        Assertions.assertNull(clientStorage.load(client.id))
    }

    @Test
    fun toPrincipal() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val principal = client.toPrincipal()

        assertEquals(client.id.toString(), principal.id)
        assertEquals(client.id, principal.clientId)
        assertEquals(client.getScope(deep = true).toSet(), principal.scope)
    }

    companion object {
        private val helper = MongoTestHelper()

        val mongoTemplate: ReactiveMongoTemplate
            get() = helper.mongoTemplate

        @BeforeAll
        @JvmStatic
        fun setUpAll() = helper.setUp()

        @AfterAll
        @JvmStatic
        fun tearDownAll() = helper.tearDown()
    }
}
