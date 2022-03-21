package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ClientInfosMapper(
    private val clientInfoMapper: ClientInfoMapper
) : Mapper<Collection<Client>, Collection<ClientInfo>> {
    override val sourceType = object : TypeReference<Collection<Client>>() {}
    override val targetType = object : TypeReference<Collection<ClientInfo>>() {}

    override suspend fun map(source: Collection<Client>): Collection<ClientInfo> {
        return source.map { clientInfoMapper.map(it) }
    }
}
