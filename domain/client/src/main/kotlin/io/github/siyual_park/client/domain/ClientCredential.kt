package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientCredentialData
import io.github.siyual_park.client.repository.ClientCredentialDataRepository
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull

class ClientCredential(
    value: ClientCredentialData,
    clientCredentialDataRepository: ClientCredentialDataRepository
) : Persistence<ClientCredentialData, Long>(value, clientCredentialDataRepository) {
    val id by proxyNotNull(root, ClientCredentialData::id)
    val clientId by proxy(root, ClientCredentialData::clientId)

    val createdAt by proxy(root, ClientCredentialData::createdAt)
    val updatedAt by proxy(root, ClientCredentialData::updatedAt)

    fun check(secret: String): Boolean {
        return root[ClientCredentialData::secret] == secret
    }

    fun set(secret: String) {
        root[ClientCredentialData::secret] = secret
    }
}
