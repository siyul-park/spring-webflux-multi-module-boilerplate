package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialDataRepository
import io.github.siyual_park.client.repository.ClientDataRepository
import io.github.siyual_park.client.repository.ClientScopeDataRepository
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryableLoader
import io.github.siyual_park.persistence.SimpleQueryableLoader
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class ClientStorage(
    private val clientDataRepository: ClientDataRepository,
    private val clientCredentialDataRepository: ClientCredentialDataRepository,
    private val clientScopeDataRepository: ClientScopeDataRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : QueryableLoader<Client, ULID> by SimpleQueryableLoader(
    clientDataRepository,
    ClientMapper(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
    ClientsMapper(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
) {
    private val random = SecureRandom.getInstance("SHA1PRNG")

    private val clientMapper = ClientMapper(clientDataRepository, clientCredentialDataRepository, clientScopeDataRepository, scopeTokenStorage)

    private val confidentialClientScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("confidential(client):pack")
    }
    private val publicClientScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("public(client):pack")
    }

    init {
        random.setSeed(random.generateSeed(128))
    }

    suspend fun save(payload: CreateClientPayload): Client {
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

    suspend fun load(name: String): Client? {
        return load(where(ClientData::name).`is`(name))
    }

    private suspend fun createClient(payload: CreateClientPayload): Client {
        return ClientData(payload.name, payload.type, payload.origin)
            .apply { if (payload.id != null) id = payload.id }
            .let { clientDataRepository.create(it) }
            .let { clientMapper.map(it) }
    }

    private suspend fun createCredential(client: Client): ClientCredentialData? {
        if (client.isPublic()) {
            return null
        }

        return clientCredentialDataRepository.create(
            ClientCredentialData(
                clientId = client.id,
                secret = generateRandomSecret()
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

    private fun generateRandomSecret(): String {
        val length = 32
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }
        return stringBuilder.toString()
    }
}

suspend fun ClientStorage.loadOrFail(name: String): Client {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
