package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientInfoMapper : Mapper<Client, ClientInfo> {
    override val sourceType = object : TypeReference<Client>() {}
    override val targetType = object : TypeReference<ClientInfo>() {}

    override suspend fun map(source: Client): ClientInfo {
        return ClientInfo(
            id = source.id!!,
            name = source.name,
            type = source.type,
            origin = source.origin,
            createdAt = source.raw().createdAt!!,
            updatedAt = source.raw().updatedAt,
            deletedAt = source.raw().deletedAt
        )
    }
}
