package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.TokenEntity
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.data.repository.updateById
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull
import io.github.siyual_park.ulid.ULID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toSet
import java.time.Instant

class Token(
    entity: TokenEntity,
    private val tokenEntityRepository: TokenEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
) : Persistence<TokenEntity, ULID>(entity, tokenEntityRepository), Authorizable {
    val id by proxyNotNull(root, TokenEntity::id)
    val type by proxy(root, TokenEntity::type)
    val signature by proxy(root, TokenEntity::signature)
    val claims by proxy(root, TokenEntity::claims)

    val createdAt by proxy(root, TokenEntity::createdAt)
    val updatedAt by proxy(root, TokenEntity::updatedAt)
    private val expiredAt by proxy(root, TokenEntity::expiredAt)

    fun isActivated(): Boolean {
        val expiredAt = expiredAt ?: return true
        val now = Instant.now()
        return expiredAt.isAfter(now)
    }

    operator fun get(key: String): Any? {
        return claims[key]
    }

    operator fun set(key: String, value: Any?) {
        val claims = root[TokenEntity::claims].toMutableMap()
        if (value == null) {
            claims.remove(key)
        } else {
            claims[key] = value
        }
        root[TokenEntity::claims] = claims
    }

    override suspend fun has(scopeToken: ScopeToken): Boolean {
        val scope = getScope().toSet()
        return scope.contains(scopeToken)
    }

    override suspend fun grant(scopeToken: ScopeToken) {
        tokenEntityRepository.updateById(id) {
            val claims = it.claims.toMutableMap()
            val scopeTokenIds = claims.getScopeTokenIds().toMutableList()
            scopeTokenIds.add(scopeToken.id)
            claims["scope"] = scopeTokenIds.map { it.toString() }
            it.claims = claims
        }?.let { root.raw(it) }
    }

    override suspend fun revoke(scopeToken: ScopeToken) {
        tokenEntityRepository.updateById(id) {
            val claims = it.claims.toMutableMap()
            val scopeTokenIds = claims.getScopeTokenIds().toMutableSet()
            scopeTokenIds.remove(scopeToken.id)
            claims["scope"] = scopeTokenIds.map { it.toString() }
            it.claims = claims
        }?.let { root.raw(it) }
    }

    fun getScope(deep: Boolean = true): Flow<ScopeToken> {
        return flow {
            val claims = root[TokenEntity::claims]
            val scopeTokenIds = claims.getScopeTokenIds()
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

    private fun Map<String, Any>.getScopeTokenIds(): List<ULID> {
        val scope = get("scope") as? Collection<*> ?: emptyList<Any>()
        return scope
            .filterIsInstance<String>()
            .map { ULID.fromString(it) }
    }
}
