package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenMapper
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.migration.CreateScopeRelation
import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateToken
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientEntityRepository
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
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
            .register(CreateClientScope(entityOperations))
    }

    protected val scopeRelationEntityRepository = ScopeRelationEntityRepository(entityOperations)
    protected val scopeTokenEntityRepository = ScopeTokenEntityRepository(entityOperations)
    protected val clientEntityRepository = ClientEntityRepository(entityOperations)
    protected val clientScopeEntityRepository = spyk(ClientScopeEntityRepository(entityOperations))

    protected val scopeTokenMapper = ScopeTokenMapper(scopeTokenEntityRepository, scopeRelationEntityRepository)
    protected val scopeTokenStorage = ScopeTokenStorage(scopeTokenEntityRepository, scopeTokenMapper)
    protected val clientStorage = ClientStorage(clientEntityRepository, clientScopeEntityRepository, scopeTokenStorage)

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            scopeTokenStorage.upsert("confidential(client):pack")
            scopeTokenStorage.upsert("public(client):pack")
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
