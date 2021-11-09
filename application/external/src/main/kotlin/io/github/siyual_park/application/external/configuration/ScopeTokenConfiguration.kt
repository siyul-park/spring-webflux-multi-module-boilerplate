package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
        val useScope = ScopeToken(name = "user")

        scopeTokenGenerator
            .register(useScope)

            .register(ScopeToken(name = "access-token:create"), listOf(useScope))
            .register(ScopeToken(name = "refresh-token:create"), listOf(useScope))

            .register(ScopeToken(name = "user:read.self"), listOf(useScope))
            .register(ScopeToken(name = "user:delete.self"), listOf(useScope))
            .register(ScopeToken(name = "user:delete"))
    }
}
