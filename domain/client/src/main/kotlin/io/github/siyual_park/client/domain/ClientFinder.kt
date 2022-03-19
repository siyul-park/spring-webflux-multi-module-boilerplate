package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.data.repository.r2dbc.findOneOrFail
import io.github.siyual_park.reader.finder.FilteredFinder
import org.springframework.stereotype.Component

@Component
class ClientFinder(
    private val clientRepository: ClientRepository
) : FilteredFinder<Client, Long>(clientRepository, where(Client::deletedAt).isNull) {
    suspend fun findByNameOrFail(name: String): Client {
        return clientRepository.findOneOrFail(applyFilter(where(Client::name).`is`(name)))
    }

    suspend fun findByName(name: String): Client? {
        return clientRepository.findOne(applyFilter(where(Client::name).`is`(name)))
    }
}
