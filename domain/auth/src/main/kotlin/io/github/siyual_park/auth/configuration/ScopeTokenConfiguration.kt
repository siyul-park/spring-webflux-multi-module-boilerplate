package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order

@Configuration
class ScopeTokenConfiguration(
    private val scopeTokenStorage: ScopeTokenStorage
) {

    @EventListener(ApplicationReadyEvent::class)
    @Order(10)
    fun generate() = runBlocking {
        scopeTokenStorage.upsert(CreateScopeTokenPayload(name = "access-token:create"))
        scopeTokenStorage.upsert(CreateScopeTokenPayload(name = "refresh-token:create"))
    }
}
