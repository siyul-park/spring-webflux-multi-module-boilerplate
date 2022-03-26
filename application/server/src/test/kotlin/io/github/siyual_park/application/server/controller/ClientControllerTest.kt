package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyNameFactory
import io.github.siyual_park.application.server.gateway.ClientControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.util.Presence
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.Optional

@IntegrationTest
class ClientControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val clientControllerGateway: ClientControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
    private val scopeTokenFactory: ScopeTokenFactory
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
                DummyCreateClientRequest.Template(
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
    fun `POST clients, status = 400`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:create")
        )

        val request = DummyCreateClientRequest.create(
            DummyCreateClientRequest.Template(
                name = Presence.ofNullable(DummyNameFactory.create(25))
            )
        )

        val response = clientControllerGateway.create(request)
        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `POST clients, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:create")
        )

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

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:read")
        )

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

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients[self]:read")
        )

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

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients[self]:read")
        )

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
    fun `GET clients_{client-id}, status = 200`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:read")
        )

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
    fun `GET clients_{client-id}, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:read")
        )

        val response = clientControllerGateway.read(otherClient.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH clients_{client-id}, status = 200`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:update")
        )

        val name = DummyNameFactory.create(10)
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
    fun `PATCH clients_{client-id}, status = 400, when name is null`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:update")
        )

        val request = UpdateClientRequest(
            name = Optional.empty(),
        )
        val response = clientControllerGateway.update(otherClient.id, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `PATCH clients_{client-id}, status = 400, when name is excess size`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients:update")
        )

        val name = DummyNameFactory.create(25)
        val request = UpdateClientRequest(
            name = Optional.of(name),
        )
        val response = clientControllerGateway.update(otherClient.id, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `PATCH clients_{client-id}, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:update")
        )

        val request = UpdateClientRequest(
            name = Optional.of(DummyNameFactory.create(10)),
            origin = Optional.of(otherClient.origin)
        )
        val response = clientControllerGateway.update(otherClient.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE clients_{client-id}, status = 200`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

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
    fun `DELEATE clients_{client-id}, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients:delete")
        )

        val response = clientControllerGateway.delete(otherClient.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET clients_{self-id}_scope, status = 200`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients[self].scope:read"),
            pop = listOf("clients.scope:read")
        )

        val response = clientControllerGateway.readScope(client.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseScope = response.responseBody.asFlow().toList().sortedBy { it.id }
        val scope = client.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, responseScope.map { it.id })
    }

    @Test
    fun `GET clients_{self-id}_scope, status = 403`() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = client.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients[self].scope:read")
        )

        val response = clientControllerGateway.readScope(client.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET clients_{client-id}_scope, status = 200`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients.scope:read")
        )

        val response = clientControllerGateway.readScope(otherClient.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseScope = response.responseBody.asFlow().toList().sortedBy { it.id }
        val scope = otherClient.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, responseScope.map { it.id })
    }

    @Test
    fun `GET clients_{client-id}_scope, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients.scope:read")
        )

        val response = clientControllerGateway.readScope(otherClient.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `POST clients_{client-id}_scope, status = 201`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients.scope:create")
        )

        val request = GrantScopeRequest(id = scope.id)
        val response = clientControllerGateway.grantScope(otherClient.id, request)

        assertEquals(HttpStatus.CREATED, response.status)

        val responseScope = response.responseBody.awaitSingle()

        assertEquals(scope.id, responseScope.id)
        assertTrue(otherClient.has(scope))
    }

    @Test
    fun `POST clients_{client-id}_scope, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients.scope:create")
        )

        val request = GrantScopeRequest(id = scope.id)
        val response = clientControllerGateway.grantScope(otherClient.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELETE clients_{client-id}_scope, status = 204`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        otherClient.grant(scope)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("clients.scope:delete")
        )

        val response = clientControllerGateway.revokeScope(otherClient.id, scope.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)
        assertFalse(otherClient.has(scope))
    }

    @Test
    fun `DELETE clients_{client-id}_scope, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        otherClient.grant(scope)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("clients.scope:delete")
        )

        val response = clientControllerGateway.revokeScope(otherClient.id, scope.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
