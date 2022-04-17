package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.data.repository.r2dbc.where
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import io.github.siyual_park.ulid.ULID
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component

@Component
class ClientStorage(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper
) : R2DBCStorage<Client, ULID> by SimpleR2DBCStorage(
    clientRepository,
    { clientMapper.map(it) }
) {
    suspend fun load(name: String): Client? {
        return load(where(ClientData::name).`is`(name))
    }
}

suspend fun ClientStorage.loadOrFail(name: String): Client {
    return load(name) ?: throw EmptyResultDataAccessException(1)
}
