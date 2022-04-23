package io.github.siyual_park.application.server.auth.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeStrategy
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.ulid.ULID
import org.springframework.stereotype.Component

@Component
@AuthorizeMapping(ScopeMatchAuthorizeFilter::class)
@ScopeMapping(
    [
        "clients[self]:read",
        "clients[self]:update",
        "clients[self]:delete",
        "clients[self].scope:read"
    ]
)
class ClientIdMatchAuthorizeStrategy : AuthorizeStrategy {
    override suspend fun authorize(
        principal: Principal,
        scopeToken: ScopeToken,
        targetDomainObject: Any?
    ): Boolean {
        val clientEntity = principal as? ClientEntity ?: return false
        val id = targetDomainObject as? ULID ?: return true

        return clientEntity.clientId == id
    }
}
