package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.repository.ClientEntityRepository
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
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
    private val clientEntityRepository: ClientEntityRepository,
    private val clientScopeEntityRepository: ClientScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : QueryableLoader<Client, ULID> by SimpleQueryableLoader(
    clientEntityRepository,
    ClientMapper(clientEntityRepository, clientScopeEntityRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
    ClientsMapper(clientEntityRepository, clientScopeEntityRepository, scopeTokenStorage).let { mapper -> { mapper.map(it) } },
) {
    private val random = SecureRandom.getInstance("SHA1PRNG")

    private val clientMapper = ClientMapper(clientEntityRepository, clientScopeEntityRepository, scopeTokenStorage)

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
        val client = ClientEntity(
            name = payload.name,
            origins = payload.origins,
            secret = if (payload.type == ClientType.CONFIDENTIAL) {
                generateRandomSecret()
            } else {
                null
            }
        )
            .apply { if (payload.id != null) id = payload.id }
            .let { clientEntityRepository.create(it) }
            .let { clientMapper.map(it) }
            .also { it.link() }

        if (payload.scope == null) {
            val scope = if (client.type == ClientType.CONFIDENTIAL) {
                confidentialClientScope.get()
            } else {
                publicClientScope.get()
            }
            client.grant(scope)
        } else {
            payload.scope.forEach {
                client.grant(it)
            }
        }

        return client
    }

    suspend fun load(name: String): Client? {
        return load(where(ClientEntity::name).`is`(name))
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
