package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.client.domain.ClientCredentialFinder
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientDetailInfoMapper(
    private val clientCredentialFinder: ClientCredentialFinder
) : Mapper<Client, ClientDetailInfo> {
    override val sourceType = object : TypeReference<Client>() {}
    override val targetType = object : TypeReference<ClientDetailInfo>() {}

    override suspend fun map(source: Client): ClientDetailInfo {
        val secret = if (source.isConfidential()) {
            clientCredentialFinder.findByClientOrFail(source).secret
        } else null

        return ClientDetailInfo(
            id = source.id!!,
            name = source.name,
            type = source.type,
            origin = source.origin,
            secret = secret,
            createdAt = source.createdAt!!,
            updatedAt = source.updatedAt,
            deletedAt = source.deletedAt
        )
    }
}
