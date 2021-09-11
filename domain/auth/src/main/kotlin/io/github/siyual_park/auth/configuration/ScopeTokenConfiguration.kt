package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.ScopeTokenGenerator
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class ScopeTokenConfiguration(
    private val scopeTokenGenerator: ScopeTokenGenerator
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(10)
    fun generate() {
        runBlocking {
            scopeTokenGenerator.generate()
        }
    }
}
