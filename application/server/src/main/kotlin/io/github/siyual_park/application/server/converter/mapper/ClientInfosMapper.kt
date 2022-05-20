package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.presentation.project.Projection
import org.springframework.stereotype.Component

@Component
class ClientInfosMapper(
    private val mapperContext: MapperContext
) : Mapper<Projection<Collection<Client>>, Collection<ClientInfo>> {
    override val sourceType = object : TypeReference<Projection<Collection<Client>>>() {}
    override val targetType = object : TypeReference<Collection<ClientInfo>>() {}

    override suspend fun map(source: Projection<Collection<Client>>): Collection<ClientInfo> {
        return source.value.map { mapperContext.map(Projection(it, source.node)) }
    }
}
