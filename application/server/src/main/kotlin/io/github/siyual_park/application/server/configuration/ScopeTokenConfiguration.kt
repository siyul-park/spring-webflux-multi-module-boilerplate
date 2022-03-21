package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
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
    @Order(11)
    fun generate() = runBlocking {
        val userScope = scopeTokenFactory.upsert(name = "user:pack")
        val clientScope = scopeTokenFactory.upsert(name = "client:pack")

        scopeTokenFactory.upsert(name = "token:create").also { grant(it, listOf(userScope)) }
        scopeTokenFactory.upsert(name = "access-token:create").also { grant(it, listOf(userScope, clientScope)) }
        scopeTokenFactory.upsert(name = "refresh-token:create").also { grant(it, listOf(userScope)) }

        scopeTokenFactory.upsert(name = "users:create").also { grant(it, listOf(clientScope)) }
        scopeTokenFactory.upsert(name = "users:read").also { grant(it, listOf(userScope, clientScope)) }
        scopeTokenFactory.upsert(name = "users:update")
        scopeTokenFactory.upsert(name = "users:delete")
        scopeTokenFactory.upsert(name = "users[self]:read").also { grant(it, listOf(userScope)) }
        scopeTokenFactory.upsert(name = "users[self]:update").also { grant(it, listOf(userScope)) }
        scopeTokenFactory.upsert(name = "users[self]:delete").also { grant(it, listOf(userScope)) }

        scopeTokenFactory.upsert(name = "clients:create")
        scopeTokenFactory.upsert(name = "clients:read").also { grant(it, listOf(userScope, clientScope)) }
        scopeTokenFactory.upsert(name = "clients:update")
        scopeTokenFactory.upsert(name = "clients:delete")
        scopeTokenFactory.upsert(name = "clients[self]:read").also { grant(it, listOf(userScope, clientScope)) }
        scopeTokenFactory.upsert(name = "clients[self]:update")
        scopeTokenFactory.upsert(name = "clients[self]:delete")
    }

    private suspend fun grant(child: ScopeToken, parents: List<ScopeToken>) {
        parents.forEach {
            if (!it.has(child)) {
                it.grant(child)
            }
        }
    }
}
