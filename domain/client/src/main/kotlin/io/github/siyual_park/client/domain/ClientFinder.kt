package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.reader.finder.Finder
import io.github.siyual_park.reader.finder.FinderAdapter
import org.springframework.stereotype.Component

@Component
class ClientFinder(
    private val clientRepository: ClientRepository
) : Finder<Client, Long> by FinderAdapter(clientRepository) {
    suspend fun findByNameOrFail(name: String): Client {
        return clientRepository.findByNameOrFail(name)
    }

    suspend fun findByName(name: String): Client? {
        return clientRepository.findByName(name)
    }
}
