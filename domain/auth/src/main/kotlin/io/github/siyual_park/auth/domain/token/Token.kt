package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.data.repository.updateById
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toSet
import java.time.Instant

class Token(
    value: TokenData,
    private val tokenRepository: TokenRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    eventPublisher: EventPublisher
) : Persistence<TokenData, ULID>(value, tokenRepository, eventPublisher), Authorizable {
    val id by proxy(root, TokenData::id)
    val claims by proxy(root, TokenData::claims)
    private val expiredAt by proxy(root, TokenData::expiredAt)

    operator fun <T> get(key: String): T? {
        return claims[key] as? T
    }

    fun isActivated(): Boolean {
        val expiredAt = expiredAt ?: return true
        val now = Instant.now()
        return expiredAt.isBefore(now)
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        val scope = getScope().toSet()
        return scope.contains(scopeToken)
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        tokenRepository.updateById(id) {
            val claims = it.claims.toMutableMap()
            val scopeTokenIds = getScopeTokenIds(claims).toMutableList()
            scopeTokenIds.add(scopeToken.id)
            claims["scope"] = scopeTokenIds.map { it.toString() }
            it.claims = claims
        }?.let { root.raw(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        tokenRepository.updateById(id) {
            val claims = it.claims.toMutableMap()
            val scopeTokenIds = getScopeTokenIds(claims).toMutableSet()
            scopeTokenIds.remove(scopeToken.id)
            claims["scope"] = scopeTokenIds.map { it.toString() }
            it.claims = claims
        }?.let { root.raw(it) }
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val claims = root[TokenData::claims]
            val scopeTokenIds = getScopeTokenIds(claims)
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

    private fun getScopeTokenIds(claims: Map<String, Any>): List<ULID> {
        val scope = (claims["scope"] as? Collection<*>) ?: emptyList<Any>()
        return scope
            .filterIsInstance<String>()
            .map { ULID.fromString(it) }
    }
}
