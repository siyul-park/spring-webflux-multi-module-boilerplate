package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence

class ClientCredential(
    value: ClientCredentialData,
    clientCredentialRepository: ClientCredentialRepository,
    eventPublisher: EventPublisher
) : Persistence<ClientCredentialData, Long>(value, clientCredentialRepository, eventPublisher) {
    val id: Long?
        get() = root[ClientCredentialData::id]

    val clientId: Long
        get() = root[ClientCredentialData::clientId]

    fun checkSecret(secret: String): Boolean {
        return root[ClientCredentialData::secret] == secret
    }

    fun setSecret(secret: String) {
        root[ClientCredentialData::secret] = secret
    }
}
