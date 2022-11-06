package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.auth.ClientPrincipal
import io.github.siyual_park.client.entity.ClientAssociable
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.client.entity.ClientScopeEntity
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.repository.ClientEntityRepository
import io.github.siyual_park.client.repository.ClientScopeEntityRepository
import io.github.siyual_park.data.aggregation.FetchContext
import io.github.siyual_park.data.aggregation.get
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.where
import io.github.siyual_park.data.repository.findOneOrFail
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.PersistenceSynchronization
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

class Client(
    entity: ClientEntity,
    clientEntityRepository: ClientEntityRepository,
    private val clientScopeEntityRepository: ClientScopeEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    fetchContext: FetchContext
) : Persistence<ClientEntity, ULID>(entity, clientEntityRepository), ClientAssociable, Authorizable {
    val id by proxy(root, ClientEntity::id)
    var name by proxy(root, ClientEntity::name)
    var origins by proxy(root, ClientEntity::origins)
    var secret by proxy(root, ClientEntity::secret)

    val type: ClientType
        get() = if (secret != null) {
            ClientType.CONFIDENTIAL
        } else {
            ClientType.PUBLIC
        }

    val createdAt by proxy(root, ClientEntity::createdAt)
    val updatedAt by proxy(root, ClientEntity::updatedAt)

    override val clientId by proxy(root, ClientEntity::id)

    private val scopeContext = fetchContext.get(clientScopeEntityRepository)
    private val scopeFetcher = scopeContext.join(
        where(ClientScopeEntity::clientId).`is`(clientId)
    )

    init {
        synchronize(
            object : PersistenceSynchronization {
                override suspend fun beforeClear() {
                    scopeFetcher.clear()
                    clientScopeEntityRepository.deleteAllByClientId(id)
                }
            }
        )
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        return clientScopeEntityRepository.exists(
            where(ClientScopeEntity::clientId).`is`(id)
                .and(where(ClientScopeEntity::scopeTokenId).`is`(scopeToken.id))
        )
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        clientScopeEntityRepository.create(
            ClientScopeEntity(
                clientId = id,
                scopeTokenId = scopeToken.id
            )
        ).also { scopeContext.clear(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        clientScopeEntityRepository.findOneOrFail(
            where(ClientScopeEntity::clientId).`is`(id)
                .and(where(ClientScopeEntity::scopeTokenId).`is`(scopeToken.id))
        ).also {
            scopeContext.clear(it)
            clientScopeEntityRepository.delete(it)
        }
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val scopeTokenIds = scopeFetcher.fetch()
                .map { it.scopeTokenId }
                .toList()

            scopeTokenStorage.load(scopeTokenIds)
                .collect {
                    if (deep) {
                        emitAll(it.resolve())
                    } else {
                        emit(it)
                    }
                }
        }
    }

    suspend fun toPrincipal(
        push: List<ScopeToken> = emptyList(),
        pop: List<ScopeToken> = emptyList()
    ): ClientPrincipal {
        val myScope = getScope().toList()
        val scope = mutableSetOf<ScopeToken>()

        scope.addAll(myScope.filter { token -> pop.firstOrNull { it.id == token.id } == null })
        scope.addAll(push.toList())

        return ClientPrincipal(
            id = id,
            clientId = clientId,
            scope = scope.toSet()
        )
    }
}
