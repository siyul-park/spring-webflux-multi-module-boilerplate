package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.SuspendSecurityContextHolder
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.presentation.project.ProjectNode
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.presentation.project.project
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientInfoMapper(
    private val mapperContext: MapperContext,
    private val authorizator: Authorizator,
) : Mapper<Projection<Client>, ClientInfo> {
    override val sourceType = object : TypeReference<Projection<Client>>() {}
    override val targetType = object : TypeReference<ClientInfo>() {}

    override suspend fun map(source: Projection<Client>): ClientInfo {
        val node = source.node
        val value = source.value
        val raw = value.raw()
        return ClientInfo(
            id = node.project(ClientInfo::id) { raw.id },
            name = node.project(ClientInfo::name) { raw.name },
            type = node.project(ClientInfo::type) { raw.type },
            origin = node.project(ClientInfo::origin) { raw.origin },
            scope = node.project(ClientInfo::scope) { getScope(value, it) },
            createdAt = node.project(ClientInfo::createdAt) { raw.createdAt },
            updatedAt = node.project(ClientInfo::updatedAt) { raw.updatedAt },
        )
    }

    private suspend fun getScope(value: Client, node: ProjectNode): Collection<ScopeTokenInfo>? {
        return if (authorize(value)) {
            mapperContext.map(Projection(value.getScope(deep = false).toList() as Collection<ScopeToken>, node))
        } else {
            null
        }
    }

    private suspend fun authorize(source: Client): Boolean {
        val principal = SuspendSecurityContextHolder.getPrincipal() ?: return false
        return authorizator.authorize(
            principal,
            listOf("clients[self].scope:read", "clients.scope:read"),
            listOf(source.id, null)
        )
    }
}
