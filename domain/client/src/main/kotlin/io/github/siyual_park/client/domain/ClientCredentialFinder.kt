package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientCredential
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.search.finder.Finder
import io.github.siyual_park.search.finder.FinderAdapter
import org.springframework.stereotype.Component

@Component
class ClientCredentialFinder(
    private val clientCredentialRepository: ClientCredentialRepository,
) : Finder<ClientCredential, Long> by FinderAdapter(clientCredentialRepository) {
    suspend fun findByClientOrFail(client: Client): ClientCredential {
        return clientCredentialRepository.findByClientOrFail(client)
    }

    suspend fun findByClient(client: Client): ClientCredential? {
        return clientCredentialRepository.findByClient(client)
    }
}
