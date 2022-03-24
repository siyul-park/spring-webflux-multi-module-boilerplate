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
        val publicClientScope = scopeTokenFactory.upsert(name = "public(client):pack")
        val confidentialClientScope = scopeTokenFactory.upsert(name = "confidential(client):pack")

        scopeTokenFactory.upsert(name = "token:create").also { grant(it, listOf(confidentialClientScope, publicClientScope)) }
        scopeTokenFactory.upsert(name = "access-token:create").also { grant(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenFactory.upsert(name = "refresh-token:create").also { grant(it, listOf(userScope)) }

        scopeTokenFactory.upsert(name = "users:create").also { grant(it, listOf(confidentialClientScope, publicClientScope)) }
        scopeTokenFactory.upsert(name = "users:read").also { grant(it, listOf(userScope, confidentialClientScope)) }
        scopeTokenFactory.upsert(name = "users:update")
        scopeTokenFactory.upsert(name = "users:delete")
        scopeTokenFactory.upsert(name = "users[self]:read").also { grant(it, listOf(userScope)) }
        scopeTokenFactory.upsert(name = "users[self]:update").also { grant(it, listOf(userScope)) }
        scopeTokenFactory.upsert(name = "users[self]:delete").also { grant(it, listOf(userScope)) }

        scopeTokenFactory.upsert(name = "clients:create")
        scopeTokenFactory.upsert(name = "clients:read")
        scopeTokenFactory.upsert(name = "clients:update")
        scopeTokenFactory.upsert(name = "clients:delete")
        scopeTokenFactory.upsert(name = "clients[self]:read").also { grant(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenFactory.upsert(name = "clients[self]:update").also { grant(it, listOf(confidentialClientScope)) }
        scopeTokenFactory.upsert(name = "clients[self]:delete").also { grant(it, listOf(confidentialClientScope)) }

        scopeTokenFactory.upsert(name = "users.scope:read")
        scopeTokenFactory.upsert(name = "users.scope:create")
        scopeTokenFactory.upsert(name = "users.scope:delete")
        scopeTokenFactory.upsert(name = "users[self].scope:read").also { grant(it, listOf(userScope)) }

        scopeTokenFactory.upsert(name = "clients.scope:read")
        scopeTokenFactory.upsert(name = "clients.scope:create")
        scopeTokenFactory.upsert(name = "clients.scope:delete")
        scopeTokenFactory.upsert(name = "clients[self].scope:read").also { grant(it, listOf(userScope, confidentialClientScope, publicClientScope)) }

        scopeTokenFactory.upsert(name = "scope:read").also { grant(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenFactory.upsert(name = "scope.children:create")
        scopeTokenFactory.upsert(name = "scope.children:delete")
    }

    private suspend fun grant(child: ScopeToken, parents: List<ScopeToken>) {
        parents.forEach {
            if (!it.has(child)) {
                it.grant(child)
            }
        }
    }
}
