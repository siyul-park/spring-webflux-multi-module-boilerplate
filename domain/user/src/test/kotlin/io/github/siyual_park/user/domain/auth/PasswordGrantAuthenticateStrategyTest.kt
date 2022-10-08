package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientCredential
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientCredentialDataRepository
import io.github.siyual_park.client.repository.ClientDataRepository
import io.github.siyual_park.client.repository.ClientScopeDataRepository
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
    protected val clientDataRepository = ClientDataRepository(entityOperations)
    protected val clientCredentialDataRepository = ClientCredentialDataRepository(entityOperations)
    protected val clientScopeDataRepository = ClientScopeDataRepository(entityOperations,)

    protected val clientStorage = ClientStorage(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage)

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
