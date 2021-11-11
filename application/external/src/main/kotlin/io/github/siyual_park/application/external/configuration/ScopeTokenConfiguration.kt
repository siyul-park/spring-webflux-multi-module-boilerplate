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
        val clientScope = ScopeToken(name = "client")

        scopeTokenGenerator
            .register(useScope)
            .register(clientScope)

            .register(ScopeToken(name = "access-token:create"), listOf(useScope, clientScope))
            .register(ScopeToken(name = "refresh-token:create"), listOf(useScope))

            .register(ScopeToken(name = "user:create"), listOf(clientScope))
            .register(ScopeToken(name = "user:read.self"), listOf(useScope))
            .register(ScopeToken(name = "user:delete.self"), listOf(useScope))
            .register(ScopeToken(name = "user:delete"))

            .register(ScopeToken(name = "client:create"), listOf(useScope))
    }
}
