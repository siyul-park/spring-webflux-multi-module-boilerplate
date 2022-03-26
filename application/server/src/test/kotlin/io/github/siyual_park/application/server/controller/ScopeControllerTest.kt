package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateScopeTokenRequest
import io.github.siyual_park.application.server.dummy.DummyCreateScopeTokenRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyNameFactory
import io.github.siyual_park.application.server.dummy.DummyScopeNameFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.ScopeControllerGateway
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.user.domain.UserFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.Optional

@IntegrationTest
class ScopeControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val scopeControllerGateway: ScopeControllerGateway,
    private val userFactory: UserFactory,
    private val scopeTokenFactory: ScopeTokenFactory
) : CoroutineTest() {

    @Test
    fun `POST scope, status = 201`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val request = DummyCreateScopeTokenRequest.create()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:create")
        )

        val response = scopeControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(request.name, responseScopeToken.name)
        assertEquals(request.description, responseScopeToken.description)
        assertFalse(responseScopeToken.system)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `POST scope, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val request = DummyCreateScopeTokenRequest.create()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:create")
        )

        val response = scopeControllerGateway.create(request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET scope, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:read")
        )

        val response = scopeControllerGateway.readAll(
            name = "eq:${scopeToken.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )

        assertEquals(HttpStatus.OK, response.status)

        val responseScope = response.responseBody.asFlow().toList()
        assertEquals(1, responseScope.size)

        val responseScopeToken = responseScope[0]
        assertEquals(scopeToken.id, responseScopeToken.id)
        assertEquals(scopeToken.name, responseScopeToken.name)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `GET scope, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:read")
        )

        val response = scopeControllerGateway.readAll(
            name = "eq:${scopeToken.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET scope_{scope-id}, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:read")
        )

        val response = scopeControllerGateway.read(scopeToken.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(scopeToken.id, responseScopeToken.id)
        assertEquals(scopeToken.name, responseScopeToken.name)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `GET scope_{scope-id}, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:read")
        )

        val response = scopeControllerGateway.read(scopeToken.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update")
        )

        val updatedName = DummyScopeNameFactory.create(10)
        val request = UpdateScopeTokenRequest(
            name = Optional.of(updatedName)
        )
        val response = scopeControllerGateway.update(scopeToken.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(scopeToken.id, responseScopeToken.id)
        assertEquals(updatedName, responseScopeToken.name)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = DummyNameFactory.create(10)
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:update")
        )

        val updatedName = DummyScopeNameFactory.create(10)
        val request = UpdateScopeTokenRequest(
            name = Optional.of(updatedName)
        )
        val response = scopeControllerGateway.update(scopeToken.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `POST scope_{scope-id}_children, status = 201`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope.children:create")
        )

        val request = GrantScopeRequest(id = child.id)
        val response = scopeControllerGateway.grantScope(parent.id, request)

        assertEquals(HttpStatus.CREATED, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(child.id, responseScopeToken.id)
        assertEquals(child.name, responseScopeToken.name)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `POST scope_{scope-id}_children, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope.children:create")
        )

        val request = GrantScopeRequest(id = child.id)
        val response = scopeControllerGateway.grantScope(parent.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET scope_{scope-id}_children, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:read")
        )

        val response = scopeControllerGateway.readChildren(parent.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseScope = response.responseBody.asFlow().toList()
        assertEquals(responseScope.size, 1)

        val responseScopeToken = responseScope[0]
        assertEquals(child.id, responseScopeToken.id)
        assertEquals(child.name, responseScopeToken.name)
        assertNotNull(responseScopeToken.createdAt)
        assertNotNull(responseScopeToken.updatedAt)
    }

    @Test
    fun `GET scope_{scope-id}_children, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:read")
        )

        val response = scopeControllerGateway.readChildren(parent.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELETE scope_{scope-id}_children, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope.children:delete")
        )

        val response = scopeControllerGateway.revokeScope(parent.id, child.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)
        assertFalse(parent.has(child))
    }

    @Test
    fun `DELETE scope_{scope-id}_children, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert("${DummyNameFactory.create(10)}:pack")
        val child = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope.children:delete")
        )

        val response = scopeControllerGateway.revokeScope(parent.id, child.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
