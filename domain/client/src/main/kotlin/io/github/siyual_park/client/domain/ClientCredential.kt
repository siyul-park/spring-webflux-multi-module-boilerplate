package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxyNotNull
import io.github.siyual_park.ulid.ULID

class ClientCredential(
    value: ClientCredentialData,
    clientCredentialRepository: ClientCredentialRepository,
    eventPublisher: EventPublisher
) : Persistence<ClientCredentialData, ULID>(value, clientCredentialRepository, eventPublisher) {
    val id by proxyNotNull(root, ClientCredentialData::id)
    val clientId by proxyNotNull(root, ClientCredentialData::clientId)

    fun checkSecret(secret: String): Boolean {
        return root[ClientCredentialData::secret] == secret
    }

    fun setSecret(secret: String) {
        root[ClientCredentialData::secret] = secret
    }
}
