package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.presentation.project.project
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientDetailInfoMapper(
    private val mapperContext: MapperContext
) : Mapper<Projection<Client>, ClientDetailInfo> {
    override val sourceType = object : TypeReference<Projection<Client>>() {}
    override val targetType = object : TypeReference<ClientDetailInfo>() {}

    override suspend fun map(source: Projection<Client>): ClientDetailInfo {
        val node = source.node
        val value = source.value
        return ClientDetailInfo(
            id = node.project(ClientDetailInfo::id) { value.id },
            name = node.project(ClientDetailInfo::name) { value.name },
            type = node.project(ClientDetailInfo::type) { value.type },
            origins = node.project(ClientDetailInfo::origins) { value.origins },
            secret = node.project(ClientDetailInfo::secret) { value.secret },
            scope = node.project(ClientDetailInfo::scope) {
                mapperContext.map(Projection(value.getScope(deep = false).toList() as Collection<ScopeToken>, it))
            },
            createdAt = node.project(ClientDetailInfo::createdAt) { value.createdAt },
            updatedAt = node.project(ClientDetailInfo::updatedAt) { value.updatedAt },
        )
    }
}
