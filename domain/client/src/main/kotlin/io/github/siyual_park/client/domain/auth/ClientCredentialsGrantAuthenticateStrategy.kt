package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.exception.SecretIncorrectException
import io.github.siyual_park.persistence.loadOrFail
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
class ClientCredentialsGrantAuthenticateStrategy(
    private val clientStorage: ClientStorage,
) : AuthenticateStrategy<ClientCredentialsGrantPayload, ClientPrincipal> {
    override val clazz = ClientCredentialsGrantPayload::class

    override suspend fun authenticate(payload: ClientCredentialsGrantPayload): ClientPrincipal? {
        val client = clientStorage.loadOrFail(payload.id)
        if (client.isConfidential()) {
            val credential = client.getCredential()
            if (payload.secret == null || !credential.checkSecret(payload.secret)) {
                throw SecretIncorrectException()
            }
        }

        return client.toPrincipal()
    }
}
