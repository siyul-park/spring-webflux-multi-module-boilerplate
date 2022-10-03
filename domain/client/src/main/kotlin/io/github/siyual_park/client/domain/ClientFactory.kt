package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.cache.SuspendLazy
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class ClientFactory(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    clientScopeRepository: ClientScopeRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) {
    private val random = SecureRandom.getInstance("SHA1PRNG")

    private val clientMapper = ClientMapper(clientRepository, clientCredentialRepository, clientScopeRepository, scopeTokenStorage)

    private val confidentialClientScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("confidential(client):pack")
    }
    private val publicClientScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("public(client):pack")
    }

    init {
        random.setSeed(random.generateSeed(128))
    }

    suspend fun create(payload: CreateClientPayload): Client {
        val client = createClient(payload)

        client.link()
        createCredential(client)

        if (payload.scope == null) {
            val scope = getDefaultScope(client)
            client.grant(scope)
        } else {
            payload.scope.forEach {
                client.grant(it)
            }
        }

        return client
    }

    private suspend fun createClient(payload: CreateClientPayload): Client {
        return ClientData(payload.name, payload.type, payload.origin)
            .apply { if (payload.id != null) id = payload.id }
            .let { clientRepository.create(it) }
            .let { clientMapper.map(it) }
    }

    private suspend fun createCredential(client: Client): ClientCredentialData? {
        if (client.isPublic()) {
            return null
        }

        return clientCredentialRepository.create(
            ClientCredentialData(
                clientId = client.id,
                secret = generateRandomSecret(32)
            )
        )
    }

    private suspend fun getDefaultScope(client: Client): ScopeToken {
        return if (client.isConfidential()) {
            confidentialClientScope.get()
        } else {
            publicClientScope.get()
        }
    }

    private fun generateRandomSecret(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }
        return stringBuilder.toString()
    }
}
