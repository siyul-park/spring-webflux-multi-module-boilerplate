package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class ScopeTokenConfiguration(
    private val scopeTokenFactory: ScopeTokenFactory
) {

    @EventListener(ApplicationReadyEvent::class)
    @Order(10)
    fun generate() = runBlocking {
        scopeTokenFactory.upsert(CreateScopeTokenPayload(name = "access-token:create"))
        scopeTokenFactory.upsert(CreateScopeTokenPayload(name = "refresh-token:create"))
    }
}
