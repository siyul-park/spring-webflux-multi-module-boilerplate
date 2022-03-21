package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.persistence.R2DBCStorage
import io.github.siyual_park.persistence.SimpleR2DBCStorage
import org.springframework.stereotype.Component

@Component
class ClientStorage(
    private val clientRepository: ClientRepository,
    private val clientMapper: ClientMapper
) : R2DBCStorage<ClientData, Long, Client> by SimpleR2DBCStorage(
    clientRepository,
    { clientMapper.map(it) }
)