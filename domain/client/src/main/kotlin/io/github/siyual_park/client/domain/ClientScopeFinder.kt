package io.github.siyual_park.client.domain

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.repository.ScopeRelationRepository
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientScopeRepository
import io.github.siyual_park.reader.finder.Finder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ClientScopeFinder(
    private val clientScopeRepository: ClientScopeRepository,
    private val scopeRelationRepository: ScopeRelationRepository,
    private val scopeTokenFinder: ScopeTokenFinder
) : Finder<ScopeToken, Long> {
    override suspend fun findById(id: Long): ScopeToken? {
        val parent = scopeTokenFinder.findByName("pack:client") ?: return null
        val scopeToken = scopeTokenFinder.findById(id) ?: return null

        return if (scopeRelationRepository.findBy(parent.id!!, scopeToken.id!!) != null) {
            scopeToken
        } else {
            null
        }
    }

    override fun findAllById(ids: Iterable<Long>): Flow<ScopeToken> {
        val idSet = ids.toSet()
        return findAll().filter { idSet.contains(it.id) }
    }

    override fun findAll(): Flow<ScopeToken> {
        return scopeTokenFinder.findAllByParent("pack:client")
    }

    fun findAllWithResolvedByClient(client: Client): Flow<ScopeToken> {
        return client.id?.let { findAllWithResolvedByClientId(it) } ?: emptyFlow()
    }

    fun findAllWithResolvedByClientId(clientId: Long): Flow<ScopeToken> {
        return flow {
            val clientScopes = clientScopeRepository.findAllByClientId(clientId).toList()
            emitAll(scopeTokenFinder.findAllWithResolved(clientScopes.map { it.scopeTokenId }))
        }
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
