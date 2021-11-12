package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientCredential
import io.github.siyual_park.client.repository.ClientCredentialRepository
import io.github.siyual_park.data.patch.AsyncPatch
import io.github.siyual_park.updater.Updater
import io.github.siyual_park.updater.UpdaterAdapter
import org.springframework.stereotype.Component

@Component
class ClientCredentialUpdater(
    private val clientCredentialRepository: ClientCredentialRepository
) : Updater<ClientCredential, Long> by UpdaterAdapter(clientCredentialRepository) {
    suspend fun updateByClient(client: Client, path: AsyncPatch<ClientCredential>): ClientCredential? {
        return clientCredentialRepository.updateByClient(client, path)
    }
}
