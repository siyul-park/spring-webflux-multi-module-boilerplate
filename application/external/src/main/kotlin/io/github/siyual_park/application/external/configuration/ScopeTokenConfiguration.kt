package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
        scopeTokenGenerator
            .register(ScopeToken(name = "user:read.self", system = true, default = true))
            .register(ScopeToken(name = "user:remove.self", system = true, default = true))
            .register(ScopeToken(name = "user:remove", system = true, default = false))
    }
}
