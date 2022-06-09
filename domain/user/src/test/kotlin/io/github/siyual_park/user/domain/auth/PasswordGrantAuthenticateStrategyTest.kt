package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.client.domain.ClientMapper
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.ClientsMapper
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientCredential
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.converter.StringToURLConverter
import io.github.siyual_park.data.converter.URLToStringConverter
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PasswordGrantAuthenticateStrategyTest : UserTestHelper(
    converters = listOf(
        URLToStringConverter(),
        StringToURLConverter()
    )
) {
    protected val clientRepository = ClientRepository(entityOperations, eventPublisher = eventEmitter)
    protected val clientCredentialRepository = ClientCredentialRepository(entityOperations, eventPublisher = eventEmitter)
    protected val clientScopeRepository = ClientScopeRepository(entityOperations, eventEmitter)

    protected val clientMapper = ClientMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)
    protected val clientsMapper = ClientsMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage, transactionalOperator, eventEmitter)

    protected val clientStorage = ClientStorage(clientRepository, clientMapper, clientsMapper)

    private val authorizationStrategy = PasswordGrantAuthenticateStrategy(userStorage, clientStorage)

    init {
        migrationManager
            .register(CreateClient(entityOperations, mongoTemplate))
            .register(CreateClientCredential(entityOperations))
            .register(CreateClientScope(entityOperations))
    }

    @Test
    fun authenticate() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        assertEquals(
            principal,
            authorizationStrategy.authenticate(PasswordGrantPayload(payload.name, payload.password, null))
        )
    }
}
