package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.authorization.Authorizable
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.Storage
import io.github.siyual_park.persistence.loadOrFail
import org.springframework.dao.EmptyResultDataAccessException

class AuthorizableContoller<T : Authorizable, ID : Any>(
    private val storage: Storage<T, ID>,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val mapperContext: MapperContext
) {
    suspend fun grantScope(id: ID, request: GrantScopeRequest): ScopeTokenInfo {
        val entity = storage.loadOrFail(id)
        val scopeToken = if (request.id != null) {
            scopeTokenStorage.loadOrFail(request.id)
        } else if (request.name != null) {
            scopeTokenStorage.loadOrFail(request.name)
        } else {
            throw EmptyResultDataAccessException(1)
        }

        entity.grant(scopeToken)

        return mapperContext.map(scopeToken)
    }

    suspend fun revokeScope(id: ID, scopeId: Long) {
        val entity = storage.loadOrFail(id)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)

        entity.revoke(scopeToken)
    }
}
