package io.github.siyual_park.client.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.client.domain.ClientCredentialUpdater
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.property.RootClientProperty
import io.github.siyual_park.data.patch.AsyncPatch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Configuration
class RootClientConfiguration(
    private val property: RootClientProperty,
    private val clientFactory: ClientFactory,
    private val clientFinder: ClientFinder,
    private val clientCredentialUpdater: ClientCredentialUpdater,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val operator: TransactionalOperator,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(100)
    fun createRootClient() = runBlocking {
        operator.executeAndAwait {
            var client = clientFinder.findByName(property.name)
            if (client == null) {
                client = CreateClientPayload(
                    property.name,
                    ClientType.CONFIDENTIAL,
                    scope = scopeTokenFinder.findAll().toList()
                ).let { clientFactory.create(it) }
            }

            if (property.secret.isNotEmpty()) {
                clientCredentialUpdater.updateByClient(
                    client,
                    AsyncPatch.with {
                        it.secret = property.secret
                    }
                )
            }
        }
    }
}
