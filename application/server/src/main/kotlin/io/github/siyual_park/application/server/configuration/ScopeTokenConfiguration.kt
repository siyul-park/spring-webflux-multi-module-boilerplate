package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
        val userScope = ScopeToken(name = "user:pack")
        val clientScope = ScopeToken(name = "client:pack")

        scopeTokenGenerator
            .register(userScope)
            .register(clientScope)

            .register(ScopeToken(name = "token:create"), listOf(clientScope))
            .register(ScopeToken(name = "access-token:create"), listOf(userScope, clientScope))
            .register(ScopeToken(name = "refresh-token:create"), listOf(userScope))

            .register(ScopeToken(name = "users:create"), listOf(clientScope))
            .register(ScopeToken(name = "users:read"), listOf(userScope, clientScope))
            .register(ScopeToken(name = "users:update"))
            .register(ScopeToken(name = "users:delete"))
            .register(ScopeToken(name = "users[self]:read"), listOf(userScope))
            .register(ScopeToken(name = "users[self]:update"), listOf(userScope))
            .register(ScopeToken(name = "users[self]:delete"), listOf(userScope))

            .register(ScopeToken(name = "clients:create"))
            .register(ScopeToken(name = "clients:read"), listOf(userScope, clientScope))
            .register(ScopeToken(name = "clients:update"))
            .register(ScopeToken(name = "clients:delete"))
            .register(ScopeToken(name = "clients[self]:read"), listOf(userScope, clientScope))
            .register(ScopeToken(name = "clients[self]:update"))
            .register(ScopeToken(name = "clients[self]:delete"))
    }
}
