package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.dummy.DummyCreateClientPayload
import io.github.siyual_park.client.dummy.DummyScopeNameFactory
import io.github.siyual_park.client.entity.ClientType
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
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.util.Optional

class ClientFactoryTest : DataTestHelper(
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
    private val clientScopeRepository = ClientScopeRepository(entityOperations, eventEmitter)

    private val scopeTokenMapper = ScopeTokenMapper(
        scopeTokenRepository,
        scopeRelationRepository,
        transactionalOperator,
        eventEmitter
    )
    private val scopeTokenStorage = ScopeTokenStorage(scopeTokenRepository, scopeTokenMapper)

    private val scopeTokenFactory = ScopeTokenFactory(scopeTokenRepository, scopeTokenMapper, eventEmitter)

    private val clientMapper = ClientMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)
    private val clientFactory = ClientFactory(clientRepository, clientCredentialRepository, clientMapper, scopeTokenStorage, transactionalOperator, eventEmitter)

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
        val payload = DummyCreateClientPayload.create(
            DummyCreateClientPayload.Template(
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
        val payload = DummyCreateClientPayload.create(
            DummyCreateClientPayload.Template(
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
        val customScope = scopeTokenFactory.upsert(DummyScopeNameFactory.create(10))

        val payload = DummyCreateClientPayload.create(
            DummyCreateClientPayload.Template(
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
