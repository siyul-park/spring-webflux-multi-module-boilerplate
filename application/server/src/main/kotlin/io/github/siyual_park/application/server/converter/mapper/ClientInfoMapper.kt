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
        val raw = source.raw()
        return ClientInfo(
            id = raw.id!!,
            name = raw.name,
            type = raw.type,
            origin = raw.origin,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt,
            deletedAt = raw.deletedAt
        )
    }
}
