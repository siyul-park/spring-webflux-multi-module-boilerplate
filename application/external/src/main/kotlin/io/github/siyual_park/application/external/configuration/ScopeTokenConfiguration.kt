package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenConfiguration {
    @Autowired(required = true)
    fun configScopeToken(scopeTokenGenerator: ScopeTokenGenerator) {
    }
}
