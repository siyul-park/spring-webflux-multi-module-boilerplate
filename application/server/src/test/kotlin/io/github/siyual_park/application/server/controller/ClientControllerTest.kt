package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.MutableClientData
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateClientRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.RandomNameFactory
import io.github.siyual_park.application.server.gateway.ClientControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import io.github.siyual_park.auth.entity.ids
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientScopeFinder
import io.github.siyual_park.client.domain.auth.ClientPrincipal
import io.github.siyual_park.client.domain.auth.ClientPrincipalExchanger
import io.github.siyual_park.client.entity.ClientType
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import io.github.siyual_park.util.Presence
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.reactive.asFlow
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
    private val clientScopeFinder: ClientScopeFinder,
    private val scopeTokenFinder: ScopeTokenFinder,
    private val scopeTokenRepository: ScopeTokenRepository
) : CoroutineTest() {
    @Test
    fun testCreateSuccess() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients:create")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
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
    fun testCreateFail() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients:create")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
        )

        val request = DummyCreateClientRequest.create()
        clientControllerGateway.create(request)

        val response = clientControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun testReadAllSuccess() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
            .let { userPrincipalExchanger.exchange(it) }

        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.readAll(
            name = "eq:${client.name}",
            sort = "asc(created_at)",
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
    fun testReadAllFail() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }
            .let { userPrincipalExchanger.exchange(it) }

        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val removeScope = scopeTokenRepository.findByNameOrFail("clients:read")

        gatewayAuthorization.setPrincipal(
            UserPrincipal(
                id = principal.id,
                scope = scopeTokenFinder.findAllWithResolved(principal.scope.ids())
                    .filter { it.id != removeScope.id }
                    .toSet()
            )
        )

        val response = clientControllerGateway.readAll(
            name = "eq:${client.name}",
            sort = "asc(created_at)",
            page = 0,
            perPage = 1
        )
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun testReadSelfSuccessWhenUseClient() = blocking {
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
        assertEquals(client.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun testReadSelfSuccessWhenUseUser() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val user = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val principal = userPrincipalExchanger.exchange(user, client)

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
    fun testReadSelfFail() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

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

    @Test
    fun testUpdateSelfSuccess() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients[self]:update")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
        )
        val request = MutableClientData(
            name = RandomNameFactory.create(10),
            origin = client.origin
        )
        val response = clientControllerGateway.updateSelf(request)

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(client.id, responseClient.id)
        assertEquals(request.name, responseClient.name)
        assertEquals(client.type, responseClient.type)
        assertEquals(request.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun testDeleteSelfSuccess() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients[self]:delete")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
        )
        val response = clientControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val clientScope = clientScopeFinder.findAllWithResolvedByClient(client).toSet()
        assertEquals(0, clientScope.size)
    }

    @Test
    fun testReadSuccess() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        gatewayAuthorization.setPrincipal(principal)

        val response = clientControllerGateway.read(otherClient.id!!)

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
    fun testUpdateSuccess() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
            .let { clientPrincipalExchanger.exchange(it) }

        val otherClient = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients:update")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
        )

        val request = MutableClientData(
            name = RandomNameFactory.create(10),
            origin = otherClient.origin
        )
        val response = clientControllerGateway.update(otherClient.id!!, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseClient = response.responseBody.awaitSingle()

        assertEquals(otherClient.id, responseClient.id)
        assertEquals(request.name, responseClient.name)
        assertEquals(otherClient.type, responseClient.type)
        assertEquals(request.origin, responseClient.origin)
        assertNotNull(responseClient.createdAt)
        assertNotNull(responseClient.updatedAt)
    }

    @Test
    fun testDeleteSuccess() = blocking {
        val payload = DummyCreateClientPayload.create()
        val client = clientFactory.create(payload)
        val principal = clientPrincipalExchanger.exchange(client)

        val otherPayload = DummyCreateClientPayload.create()
        val otherClient = clientFactory.create(otherPayload)

        val additionalScope = scopeTokenRepository.findByNameOrFail("clients:delete")

        val scope = mutableSetOf<ScopeToken>()
        scope.add(additionalScope)
        scope.addAll(principal.scope)

        gatewayAuthorization.setPrincipal(
            ClientPrincipal(
                id = principal.id,
                scope = scope
            )
        )
        val response = clientControllerGateway.delete(otherClient.id!!)

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val clientScope = clientScopeFinder.findAllWithResolvedByClient(otherClient).toSet()
        assertEquals(0, clientScope.size)
    }
}
