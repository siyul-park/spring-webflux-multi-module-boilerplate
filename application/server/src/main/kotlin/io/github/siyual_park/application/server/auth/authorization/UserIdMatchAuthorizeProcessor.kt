package io.github.siyual_park.application.server.auth.authorization

import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeProcessor
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.user.domain.auth.UserPrincipal
import org.springframework.stereotype.Component

@Component
@AuthorizeMapping(ScopeMatchAuthorizeFilter::class)
@ScopeMapping("users[self]:read users[self]:update users[self]:delete")
class UserIdMatchAuthorizeProcessor : AuthorizeProcessor<UserPrincipal> {
    override suspend fun authorize(
        principal: UserPrincipal,
        scopeToken: ScopeToken,
        targetDomainObject: Any?
    ): Boolean {
        val id = targetDomainObject as? Long ?: return true
        return principal.userId == id
    }
}
