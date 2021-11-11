package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.search.finder.Finder
import io.github.siyual_park.search.finder.FinderAdapter
import org.springframework.stereotype.Component

@Component
class ClientFinder(
    private val clientRepository: ClientRepository
) : Finder<Client, Long> by FinderAdapter(clientRepository) {
    suspend fun findByNameOrFail(name: String): Client {
        return clientRepository.findByNameOrFail(name)
    }
}
