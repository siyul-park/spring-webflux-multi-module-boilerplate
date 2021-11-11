package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.search.finder.Finder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientScopeFinder(
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeTokenFinder: ScopeTokenFinder
) : Finder<ScopeToken, Long> {
    override suspend fun findById(id: Long): ScopeToken? {
        return findAll().first { it.id == id }
    }

    override fun findAllById(ids: Iterable<Long>): Flow<ScopeToken> {
        val idSet = ids.toSet()
        return findAll().filter { idSet.contains(it.id) }
    }

    override fun findAll(): Flow<ScopeToken> {
        return scopeTokenFinder.findAllByParent("client")
    }

    fun findAllByClient(client: Client): Flow<ScopeToken> {
        return client.id?.let { findAllByClientId(it) } ?: emptyFlow()
    }

    fun findAllByClientId(clientId: Long): Flow<ScopeToken> {
        return flow {
            val clientScopes = clientScopeRepository.findAllByClientId(clientId).toList()
            emitAll(scopeTokenFinder.findAllById(clientScopes.map { it.scopeTokenId }))
        }
    }
}
