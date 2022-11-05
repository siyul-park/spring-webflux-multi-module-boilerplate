package io.github.siyual_park.application.server.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException

@Configuration
class ScopeTokenConfiguration(
    private val scopeTokenStorage: ScopeTokenStorage
) {

    @EventListener(ApplicationReadyEvent::class)
    @Order(11)
    fun generate() = runBlocking {
        val userScope = scopeTokenStorage.upsert(name = "user:pack")
        val publicClientScope = scopeTokenStorage.upsert(name = "public(client):pack")
        val confidentialClientScope = scopeTokenStorage.upsert(name = "confidential(client):pack")

        scopeTokenStorage.upsert(name = "access-token:create").also { granted(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenStorage.upsert(name = "refresh-token:create").also { granted(it, listOf(userScope)) }

        scopeTokenStorage.upsert(name = "principal[self]:read").also { granted(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenStorage.upsert(name = "principal[self]:delete").also { granted(it, listOf(userScope, confidentialClientScope, publicClientScope)) }

        scopeTokenStorage.upsert(name = "users:create").also { granted(it, listOf(confidentialClientScope, publicClientScope)) }
        scopeTokenStorage.upsert(name = "users:read").also { granted(it, listOf(userScope, confidentialClientScope)) }
        scopeTokenStorage.upsert(name = "users:update")
        scopeTokenStorage.upsert(name = "users:delete")
        scopeTokenStorage.upsert(name = "users[self]:read").also { granted(it, listOf(userScope)) }
        scopeTokenStorage.upsert(name = "users[self]:update").also { granted(it, listOf(userScope)) }
        scopeTokenStorage.upsert(name = "users[self]:delete").also { granted(it, listOf(userScope)) }

        scopeTokenStorage.upsert(name = "users.scope:create")
        scopeTokenStorage.upsert(name = "users.scope:read")
        scopeTokenStorage.upsert(name = "users.scope:delete")
        scopeTokenStorage.upsert(name = "users[self].scope:read").also { granted(it, listOf(userScope)) }

        scopeTokenStorage.upsert(name = "clients:create")
        scopeTokenStorage.upsert(name = "clients:read")
        scopeTokenStorage.upsert(name = "clients:update")
        scopeTokenStorage.upsert(name = "clients:delete")
        scopeTokenStorage.upsert(name = "clients[self]:read").also { granted(it, listOf(userScope, confidentialClientScope, publicClientScope)) }
        scopeTokenStorage.upsert(name = "clients[self]:update").also { granted(it, listOf(confidentialClientScope)) }
        scopeTokenStorage.upsert(name = "clients[self]:delete").also { granted(it, listOf(confidentialClientScope)) }

        scopeTokenStorage.upsert(name = "clients.scope:create")
        scopeTokenStorage.upsert(name = "clients.scope:read")
        scopeTokenStorage.upsert(name = "clients.scope:delete")
        scopeTokenStorage.upsert(name = "clients[self].scope:read").also { granted(it, listOf(userScope, confidentialClientScope, publicClientScope)) }

        scopeTokenStorage.upsert(name = "scope:create")
        scopeTokenStorage.upsert(name = "scope:read")
        scopeTokenStorage.upsert(name = "scope:update")
        scopeTokenStorage.upsert(name = "scope:delete")

        scopeTokenStorage.upsert(name = "scope.children:create")
        scopeTokenStorage.upsert(name = "scope.children:delete")

        scopeTokenStorage.upsert(name = "cache.status:read")
    }

    private suspend fun granted(child: ScopeToken, parents: List<ScopeToken>) {
        parents.forEach {
            try {
                it.grant(child)
            } catch (_: DataIntegrityViolationException) {
            }
        }
    }
}
