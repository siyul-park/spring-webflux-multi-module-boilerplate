package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
        val useScope = ScopeToken(name = "user", system = true)
        val systemScope = ScopeToken(name = "system", system = true)

        scopeTokenGenerator
            .register(useScope)
            .register(systemScope)

            .register(ScopeToken(name = "access-token:create", system = true), listOf(useScope, systemScope))
            .register(ScopeToken(name = "refresh-token:create", system = true), listOf(useScope, systemScope))

            .register(ScopeToken(name = "user:read.self", system = true), listOf(useScope))
            .register(ScopeToken(name = "user:delete.self", system = true), listOf(useScope))
            .register(ScopeToken(name = "user:delete", system = true))
    }
}
