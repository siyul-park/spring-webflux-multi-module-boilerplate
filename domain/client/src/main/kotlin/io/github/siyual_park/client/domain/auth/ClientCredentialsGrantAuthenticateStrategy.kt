package io.github.siyual_park.client.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.exception.SecretIncorrectException
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.reader.finder.findByIdOrFail
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = ClientCredentialsGrantPayload::class)
class ClientCredentialsGrantAuthenticateStrategy(
    private val clientFinder: ClientFinder,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientPrincipalExchanger: ClientPrincipalExchanger,
) : AuthenticateStrategy<ClientCredentialsGrantPayload, ClientPrincipal> {
    override suspend fun authenticate(payload: ClientCredentialsGrantPayload): ClientPrincipal? {
        val client = clientFinder.findByIdOrFail(payload.id)
        if (client.isConfidential()) {
            val clientCredential = clientCredentialRepository.findByClientOrFail(client)
            if (clientCredential.secret != payload.secret) {
                throw SecretIncorrectException()
            }
        }

        return clientPrincipalExchanger.exchange(client)
    }
}
