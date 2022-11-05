package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.migration.CreateClient
import io.github.siyual_park.client.migration.CreateClientScope
import io.github.siyual_park.client.repository.ClientEntityRepository
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
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
    protected val clientEntityRepository = ClientEntityRepository(entityOperations)
    protected val clientScopeEntityRepository = ClientScopeEntityRepository(entityOperations,)

    protected val clientStorage = ClientStorage(clientEntityRepository, clientScopeEntityRepository, scopeTokenStorage)

    private val authorizationStrategy = PasswordGrantAuthenticateStrategy(userStorage, clientStorage)

    init {
        migrationManager
            .register(CreateClient(entityOperations, mongoTemplate))
            .register(CreateClientScope(entityOperations))
    }

    @Test
    fun authenticate() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userStorage.save(payload)
        val principal = user.toPrincipal()

        assertEquals(
            principal,
            authorizationStrategy.authenticate(PasswordGrantPayload(payload.name, payload.password, null))
        )
    }
}
