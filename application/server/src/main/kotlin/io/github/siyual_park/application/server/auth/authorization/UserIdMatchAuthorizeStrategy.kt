package io.github.siyual_park.application.server.auth.authorization

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.authorization.AuthorizeMapping
import io.github.siyual_park.auth.domain.authorization.AuthorizeStrategy
import io.github.siyual_park.auth.domain.authorization.ScopeMapping
import io.github.siyual_park.auth.domain.authorization.ScopeMatchAuthorizeFilter
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.user.entity.UserEntity
import org.springframework.stereotype.Component

@Component
@AuthorizeMapping(ScopeMatchAuthorizeFilter::class)
@ScopeMapping("users[self]:read users[self]:update users[self]:delete")
class UserIdMatchAuthorizeStrategy : AuthorizeStrategy {
    override suspend fun authorize(
        principal: Principal,
        scopeToken: ScopeToken,
        targetDomainObject: Any?
    ): Boolean {
        val userEntity = principal as? UserEntity ?: return false
        val id = targetDomainObject as? Long ?: return true

        return userEntity.userId == id
    }
}
