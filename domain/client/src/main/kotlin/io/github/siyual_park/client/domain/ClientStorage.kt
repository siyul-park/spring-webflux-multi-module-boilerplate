package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.persistence.QueryStorage
import io.github.siyual_park.persistence.SimpleQueryStorage
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class ClientStorage(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper,
    private val clientsMapper: ClientsMapper,
) : QueryStorage<Client, ULID> by SimpleQueryStorage(
    clientRepository,
    { clientMapper.map(it) },
    { clientsMapper.map(it) }
) {
    suspend fun load(name: String): Client? {
        return load(where(ClientData::name).`is`(name))
    }
}

suspend fun ClientStorage.loadOrFail(name: String): Client {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
