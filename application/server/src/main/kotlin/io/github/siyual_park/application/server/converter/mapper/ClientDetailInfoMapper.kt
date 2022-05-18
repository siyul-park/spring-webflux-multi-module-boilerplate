package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.PrincipalProvider
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.AsyncLazy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientDetailInfoMapper(
    private val mapperContext: MapperContext,
    private val principalProvider: PrincipalProvider,
    private val authorizator: Authorizator,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<Client, ClientDetailInfo> {
    override val sourceType = object : TypeReference<Client>() {}
    override val targetType = object : TypeReference<ClientDetailInfo>() {}

    private val scopeSelfReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("clients[self].scope:read")
    }
    private val scopeReadScopeToken = AsyncLazy {
        scopeTokenStorage.loadOrFail("clients.scope:read")
    }

    override suspend fun map(source: Client): ClientDetailInfo {
        val principal = principalProvider.get()
        val scope: Collection<ScopeTokenInfo>? = if (
            principal != null &&
            authorizator.authorize(
                principal,
                listOf(scopeSelfReadScopeToken.get(), scopeReadScopeToken.get()),
                listOf(source.id, null)
            )
        ) {
            mapperContext.map(source.getScope().toList() as Collection<ScopeToken>)
        } else {
            null
        }

        val secret = getSecret(source)
        val raw = source.raw()
        return ClientDetailInfo(
            id = raw.id,
            name = raw.name,
            type = raw.type,
            origin = raw.origin,
            secret = secret,
            scope = scope,
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
