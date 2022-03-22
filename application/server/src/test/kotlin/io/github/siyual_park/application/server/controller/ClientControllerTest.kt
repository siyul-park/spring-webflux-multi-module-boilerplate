package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.RandomNameFactory
import io.github.siyual_park.application.server.gateway.ClientControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.util.Presence
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.Optional

@IntegrationTest
class ClientControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val clientControllerGateway: ClientControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory
) : CoroutineTest() {

    @Test
    fun `POST clients, status = 201`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:create")
        )

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
            assertEquals(request.origin, client.origin)
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
    fun `POST clients, status = 409`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:create")
        )

        val request = DummyCreateClientRequest.create()
        clientControllerGateway.create(request)

        val response = clientControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun `POST clients, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(principal)

        val request = DummyCreateClientRequest.create()
        val response = clientControllerGateway.create(request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET clients, status = 201`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.readAll(
            name = "eq:${client.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )

        assertEquals(HttpStatus.OK, response.status)

        val responseClients = response.responseBody.asFlow().toList()
        assertEquals(1, responseClients.size)

        val responseClient = responseClients[0]
        assertEquals(client.id, responseClient.id)
        assertEquals(client.name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertEquals(client.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `GET clients, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:read")
        )

        val response = clientControllerGateway.readAll(
            name = "eq:${client.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET clients_self, status = 200, when use client principal`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(client.id, responseClient.id)
        assertEquals(client.name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertEquals(client.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `GET clients_self, status = 200, when use user principal`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val principal = user.toPrincipal(clientEntity = client)

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(client.id, responseClient.id)
        assertEquals(client.name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertEquals(client.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `GET clients_self, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients[self]:read")
        )

        val response = clientControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH clients_self, status = 200`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(principal)

        val name = RandomNameFactory.create(10)
        val request = UpdateClientRequest(
            name = Optional.of(name)
        )
        val response = clientControllerGateway.updateSelf(request)

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(client.id, responseClient.id)
        assertEquals(name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertEquals(client.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `PATCH clients_self, status = 403`() = blocking {
        val payload = DummyCreateClientPayload.create(
            DummyCreateClientPayload.CreateClientPayloadTemplate(
                type = Presence.ofNullable(ClientType.PUBLIC)
            )
        )
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(principal)

        val request = UpdateClientRequest(
            name = Optional.of(RandomNameFactory.create(10))
        )
        val response = clientControllerGateway.updateSelf(request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELETE clients_self, status = 200`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients[self]:delete")
        )

        val response = clientControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val clientScope = client.getScope().toSet()
        assertEquals(0, clientScope.size)
    }

    @Test
    fun `DELETE clients_self, status = 204`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)
    }

    @Test
    fun `DELETE clients_self, status = 403`() = blocking {
        val payload = DummyCreateClientPayload.create(
            DummyCreateClientPayload.CreateClientPayloadTemplate(
                type = Presence.ofNullable(ClientType.PUBLIC)
            )
        )
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.deleteSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET clients_{client_id}, status = 200`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.read(otherClient.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(otherClient.id, responseClient.id)
        assertEquals(otherClient.name, responseClient.name)
        assertEquals(otherClient.type, responseClient.type)
        assertEquals(otherClient.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `GET clients_{client_id}, status = 403`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:read")
        )

        val response = clientControllerGateway.read(otherClient.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH clients_{client_id}, status = 200`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:update")
        )

        val name = RandomNameFactory.create(10)
        val request = UpdateClientRequest(
            name = Optional.of(name),
        )
        val response = clientControllerGateway.update(otherClient.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(otherClient.id, responseClient.id)
        assertEquals(name, responseClient.name)
        assertEquals(otherClient.type, responseClient.type)
        assertEquals(otherClient.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun `PATCH clients_{client_id}, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(principal)

        val request = UpdateClientRequest(
            name = Optional.of(RandomNameFactory.create(10)),
            origin = Optional.of(otherClient.origin)
        )
        val response = clientControllerGateway.update(otherClient.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE clients_{client_id}, status = 200`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:delete")
        )

        val response = clientControllerGateway.delete(otherClient.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val clientScope = otherClient.getScope().toSet()
        assertEquals(0, clientScope.size)
    }

    @Test
    fun `DELEATE clients_{client_id}, status = 403`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.delete(otherClient.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
