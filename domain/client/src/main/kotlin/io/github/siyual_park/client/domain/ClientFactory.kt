package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientScopeData
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.event.AfterSaveEvent
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.loadOrFail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.Random

@Component
class ClientFactory(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val clientMapper: ClientMapper,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
) {
    suspend fun create(payload: CreateClientPayload): Client =
        operator.executeAndAwait {
            createClient(payload).also {
                createCredential(it)
                if (payload.scope == null) {
                    createDefaultScope(it).collect()
                } else {
                    createScope(it, payload.scope).collect()
                }
            }
                .let { clientMapper.map(it) }
                .also { eventPublisher.publish(AfterSaveEvent(it)) }
        }!!

    private suspend fun createClient(payload: CreateClientPayload): ClientData {
        return clientRepository.create(ClientData(payload.name, payload.type, payload.origin))
    }

    private suspend fun createCredential(client: ClientData): ClientCredentialData? {
        if (client.type == ClientType.PUBLIC) {
            return null
        }

        return clientCredentialRepository.create(
            ClientCredentialData(
                clientId = client.id!!,
                secret = generateRandomSecret(64)
            )
        )
    }

    private suspend fun createDefaultScope(client: ClientData): Flow<ClientScopeData> {
        return flow {
            val pack = listOf(scopeTokenStorage.loadOrFail(where(ScopeTokenData::name).`is`("client:pack")))

            emitAll(createScope(client, pack))
        }
    }

    private fun createScope(client: ClientData, scope: Collection<ScopeToken>): Flow<ClientScopeData> {
        return clientScopeRepository.createAll(
            scope.map {
                ClientScopeData(
                    clientId = client.id!!,
                    scopeTokenId = it.id!!
                )
            }
        )
    }

    private fun generateRandomSecret(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%&"
        val random = Random()
        val stringBuilder = StringBuilder(length)
        for (i in 0 until length) {
            stringBuilder.append(chars[random.nextInt(chars.length)])
        }
        return stringBuilder.toString()
    }
}
