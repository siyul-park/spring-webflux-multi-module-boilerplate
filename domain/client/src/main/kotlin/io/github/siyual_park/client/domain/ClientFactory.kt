package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.data.event.AfterCreateEvent
import io.github.siyual_park.event.EventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.Random

@Component
class ClientFactory(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientMapper: ClientMapper,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
) {
    suspend fun create(payload: CreateClientPayload): Client =
        operator.executeAndAwait {
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

            eventPublisher.publish(AfterCreateEvent(client))

            client
        }!!

    private suspend fun createClient(payload: CreateClientPayload): Client {
        return ClientData(payload.name, payload.type, payload.origin)
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
                secret = generateRandomSecret(64)
            )
        )
    }

    private suspend fun getDefaultScope(client: Client): ScopeToken {
        return if (client.isConfidential()) {
            scopeTokenStorage.loadOrFail("confidential(client):pack")
        } else {
            scopeTokenStorage.loadOrFail("public(client):pack")
        }
    }

    private fun generateRandomSecret(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random()
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }
        return stringBuilder.toString()
    }
}
