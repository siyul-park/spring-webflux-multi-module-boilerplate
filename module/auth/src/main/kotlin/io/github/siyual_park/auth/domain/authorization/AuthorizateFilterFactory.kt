package io.github.siyual_park.auth.domain.authorization

import org.springframework.stereotype.Component

@Component
class AuthorizateFilterFactory {
    fun create(mapping: AuthorizeMapping): AuthorizeFilter? {
        return SimpleAuthorizeFilter(
            principal = mapping.principal.java,
            scope = mapping.scope
        )
    }
}
