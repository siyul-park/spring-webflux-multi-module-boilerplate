package io.github.siyual_park.application.server.auth.authorization

import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeProcessor
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.client.domain.auth.ClientPrincipal
import org.springframework.stereotype.Component

@Component
@AuthorizeMapping(ClientPrincipal::class, ScopeMatchAuthorizeFilter::class)
@ScopeMapping("clients[self]:read clients[self]:update clients[self]:delete")
class ClientIdMatchAuthorizeProcessor : AuthorizeProcessor<ClientPrincipal> {
    override suspend fun authorize(
        principal: ClientPrincipal,
        scopeToken: ScopeToken,
        targetDomainObject: Any?
    ): Boolean {
        val id = targetDomainObject as? Long ?: return true
        return principal.clientId == id
    }
}
