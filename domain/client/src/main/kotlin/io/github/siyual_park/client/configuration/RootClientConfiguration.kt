package io.github.siyual_park.client.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.client.domain.ClientCredentialFinder
import io.github.siyual_park.client.domain.ClientCredentialUpdater
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.domain.ClientUpdater
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.property.RootClientProperty
import io.github.siyual_park.data.patch.AsyncPatch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
    private val clientCredentialFinder: ClientCredentialFinder,
    private val clientUpdater: ClientUpdater,
    private val clientCredentialUpdater: ClientCredentialUpdater,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val operator: TransactionalOperator,
) {
    private val logger = LoggerFactory.getLogger(RootClientConfiguration::class.java)

    @EventListener(ApplicationReadyEvent::class)
    @Order(100)
    fun createRootClient() = runBlocking {
        operator.executeAndAwait {
            var client = clientFinder.findByName(property.name)
            if (client == null) {
                client = CreateClientPayload(
                    property.name,
                    ClientType.CONFIDENTIAL,
                    origin = property.origin,
                    scope = scopeTokenFinder.findAll().toList()
                )
                    .let { clientFactory.create(it) }
                    .also {
                        val credential = clientCredentialFinder.findByClientOrFail(it)
                        logger.info("Creating root client [id: ${it.id}, name: ${it.name}, secret: ${credential.secret}]")
                    }
            }

            clientUpdater.update(
                client,
                AsyncPatch.with {
                    it.origin = property.origin
                }
            )

            if (property.secret.isNotEmpty()) {
                clientCredentialUpdater.updateByClient(
                    client,
                    AsyncPatch.with {
                        it.secret = property.secret
                    }
                ).also {
                    logger.info("Updating root client [secret: ${it?.secret}]")
                }
            }
        }
    }
}
