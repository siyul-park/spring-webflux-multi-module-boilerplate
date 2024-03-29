package io.github.siyual_park.client.configuration

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.client.property.RootClientProperty
import io.github.siyual_park.ulid.ULID
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
    private val clientStorage: ClientStorage,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val operator: TransactionalOperator,
) {
    private val logger = LoggerFactory.getLogger(RootClientConfiguration::class.java)

    @EventListener(ApplicationReadyEvent::class)
    @Order(100)
    fun createRootClient() = runBlocking {
        operator.executeAndAwait {
            var client = clientStorage.load(property.name)
            if (client == null) {
                client = CreateClientPayload(
                    property.name,
                    ClientType.CONFIDENTIAL,
                    origins = listOf(property.origin),
                    scope = scopeTokenStorage.load().toList(),
                    id = if (property.id.isNotEmpty()) ULID.fromString(property.id) else null
                )
                    .let { clientStorage.save(it) }
                    .also {
                        logger.info("Creating root client [id: ${it.id}, name: ${it.name}, secret: ${it.secret}]")
                    }
            }

            client.origins = listOf(property.origin)
            if (property.secret.isNotEmpty()) {
                client.secret = property.secret
                logger.info("Updating root client [secret: ${property.secret}]")
            }
        }
    }
}
