package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.Presence
import io.github.siyual_park.application.server.gateway.ClientControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ids
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.auth.ClientPrincipal
import io.github.siyual_park.client.domain.auth.ClientPrincipalExchanger
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class ClientControllerTest@Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val clientControllerGateway: ClientControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val clientPrincipalExchanger: ClientPrincipalExchanger,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val scopeTokenRepository: ScopeTokenRepository
) : CoroutineTest() {
    @Test
    fun testCreateSuccess() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
            .let { userPrincipalExchanger.exchange(it) }

        gatewayAuthorization.setPrincipal(principal)

        ClientType.values().forEach {
            val request = DummyCreateClientRequest.create(
                DummyCreateClientRequest.CreateClientRequestTemplate(
                    type = Presence.Exist(it)
                )
            )
            val response = clientControllerGateway.create(request)

            assertEquals(HttpStatus.CREATED, response.status)

            val client = response.responseBody.awaitSingle()

            assertNotNull(client.id)
            assertEquals(request.name, client.name)
            assertEquals(request.type, client.type)
            if (request.type == ClientType.CONFIDENTIAL) {
                assertNotNull(client.secret)
            } else {
                assertEquals(client.secret, null)
            }
            assertNotNull(client.createdAt)
            assertNotNull(client.updatedAt)
        }
    }

    @Test
    fun testCreateFail() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
            .let { userPrincipalExchanger.exchange(it) }

        gatewayAuthorization.setPrincipal(principal)

        val request = DummyCreateClientRequest.create()
        clientControllerGateway.create(request)

        val response = clientControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun testReadSelfSuccess() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(client.id, responseClient.id)
        assertEquals(client.name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun testReadSelfFail() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        val removeScope = scopeTokenRepository.findByNameOrFail("clients[self]:read")

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scopeTokenFinder.findAllWithResolved(principal.scope.ids())
                    .filter { it.id != removeScope.id }
                    .toSet()
            )
        )

        val response = clientControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
