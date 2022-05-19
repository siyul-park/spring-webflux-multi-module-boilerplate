package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.getPrincipal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientInfoMapper(
    private val mapperContext: MapperContext,
    private val authorizator: Authorizator,
) : Mapper<Client, ClientInfo> {
    override val sourceType = object : TypeReference<Client>() {}
    override val targetType = object : TypeReference<ClientInfo>() {}

    override suspend fun map(source: Client): ClientInfo {
        val scope: Collection<ScopeTokenInfo>? = if (authorize(source)) {
            mapperContext.map(source.getScope(deep = false).toList() as Collection<ScopeToken>)
        } else {
            null
        }

        val raw = source.raw()
        return ClientInfo(
            id = raw.id,
            name = raw.name,
            type = raw.type,
            origin = raw.origin,
            scope = scope,
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }

    private suspend fun authorize(source: Client): Boolean {
        val principal = getPrincipal() ?: return false
        return authorizator.authorize(
            principal,
            listOf("clients[self].scope:read", "clients.scope:read"),
            listOf(source.id, null)
        )
    }
}
