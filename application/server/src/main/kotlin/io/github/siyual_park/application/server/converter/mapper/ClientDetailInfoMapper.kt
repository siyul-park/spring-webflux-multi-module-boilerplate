package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientDetailInfoMapper : Mapper<Client, ClientDetailInfo> {
    override val sourceType = object : TypeReference<Client>() {}
    override val targetType = object : TypeReference<ClientDetailInfo>() {}

    override suspend fun map(source: Client): ClientDetailInfo {
        val secret = if (source.isConfidential()) {
            source.getCredential().raw().secret
        } else null

        return ClientDetailInfo(
            id = source.id!!,
            name = source.name,
            type = source.type,
            origin = source.origin,
            secret = secret,
            createdAt = source.raw().createdAt!!,
            updatedAt = source.raw().updatedAt,
            deletedAt = source.raw().deletedAt
        )
    }
}
