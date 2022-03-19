package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.data.repository.updateById
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Instant

@Component
class ClientRemover(
    private val clientRepository: ClientRepository,
    private val clientCredentialRepository: ClientCredentialRepository,
    private val clientScopeRepository: ClientScopeRepository,
    private val operator: TransactionalOperator,
) {
    suspend fun remove(client: Client, soft: Boolean = true) {
        client.id?.let { remove(it, soft) }
    }

    suspend fun remove(clientId: Long, soft: Boolean = true) = operator.executeAndAwait {
        clientScopeRepository.deleteAllByClientId(clientId)
        clientCredentialRepository.deleteByClientId(clientId)
        if (!soft) {
            clientRepository.deleteById(clientId)
        } else {
            clientRepository.updateById(clientId) {
                if (it.deletedAt == null) {
                    it.deletedAt = Instant.now()
                }
            }
        }
    }
}
