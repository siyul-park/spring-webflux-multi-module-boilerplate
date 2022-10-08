package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationDataRepository
import io.github.siyual_park.auth.repository.ScopeTokenDataRepository
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientCredential
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientCredentialDataRepository
import io.github.siyual_park.client.repository.ClientDataRepository
import io.github.siyual_park.client.repository.ClientScopeDataRepository
import io.github.siyual_park.data.converter.StringToURLConverter
import io.github.siyual_park.data.converter.URLToStringConverter
import io.github.siyual_park.data.test.DataTestHelper
import io.github.siyual_park.data.test.MongoTestHelper
import io.mockk.spyk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

abstract class ClientTestHelper : DataTestHelper(
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

    protected val scopeRelationDataRepository = ScopeRelationDataRepository(entityOperations)
    protected val scopeTokenDataRepository = ScopeTokenDataRepository(entityOperations)
    protected val clientDataRepository = ClientDataRepository(entityOperations)
    protected val clientCredentialDataRepository = ClientCredentialDataRepository(entityOperations)
    protected val clientScopeDataRepository = spyk(ClientScopeDataRepository(entityOperations))

    protected val scopeTokenMapper = ScopeTokenMapper(scopeTokenDataRepository, scopeRelationDataRepository)
    protected val scopeTokenStorage = ScopeTokenStorage(scopeTokenDataRepository, scopeTokenMapper)
    protected val scopeTokenFactory = ScopeTokenFactory(scopeTokenDataRepository, scopeTokenMapper)

    protected val clientFactory = ClientFactory(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage)
    protected val clientStorage = ClientStorage(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenFactory.upsert("confidential(client):pack")
            scopeTokenFactory.upsert("public(client):pack")
        }
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
