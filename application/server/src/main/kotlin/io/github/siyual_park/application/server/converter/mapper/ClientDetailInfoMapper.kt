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
        val secret = getSecret(source)
        val raw = source.raw()
        return ClientDetailInfo(
            id = raw.id,
            name = raw.name,
            type = raw.type,
            origin = raw.origin,
            secret = secret,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt,
        )
    }

    private suspend fun getSecret(client: Client): String? {
        return if (client.isConfidential()) {
            client.getCredential().raw().secret
        } else {
            null
        }
    }
}
