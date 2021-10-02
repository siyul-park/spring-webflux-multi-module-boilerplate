package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import io.github.siyual_park.auth.entity.ScopeToken
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class ScopeTokenConfiguration(
    private val scopeTokenGenerator: ScopeTokenGenerator
) {
    init {
        scopeTokenGenerator
            .register(ScopeToken(name = "access-token:create", system = true))
            .register(ScopeToken(name = "refresh-token:create", system = true))
    }

    @EventListener(ApplicationReadyEvent::class)
    @Order(10)
    fun generate() {
        runBlocking {
            scopeTokenGenerator.generate()
        }
    }
}
