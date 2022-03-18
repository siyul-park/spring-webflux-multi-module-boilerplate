package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
        val useScope = ScopeToken(name = "user:pack")
        val clientScope = ScopeToken(name = "client:pack")

        scopeTokenGenerator
            .register(useScope)
            .register(clientScope)

            .register(ScopeToken(name = "token:create"), listOf(useScope, clientScope))
            .register(ScopeToken(name = "access-token:create"), listOf(useScope, clientScope))
            .register(ScopeToken(name = "refresh-token:create"), listOf(useScope))

            .register(ScopeToken(name = "users:create"), listOf(clientScope))
            .register(ScopeToken(name = "users:delete"))
            .register(ScopeToken(name = "users[self]:read"), listOf(useScope))
            .register(ScopeToken(name = "users[self]:update"), listOf(useScope))
            .register(ScopeToken(name = "users[self]:delete"), listOf(useScope))

            .register(ScopeToken(name = "clients:create"), listOf(useScope))
            .register(ScopeToken(name = "clients[self]:read"), listOf(clientScope))
    }
}
