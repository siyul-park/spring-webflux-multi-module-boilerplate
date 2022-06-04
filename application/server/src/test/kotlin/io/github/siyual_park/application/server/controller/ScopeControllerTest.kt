package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.MockCreateScopeTokenRequestFactory
import io.github.siyual_park.application.server.dto.request.UpdateScopeTokenRequest
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.ScopeControllerGateway
import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserFactory
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
class ScopeControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val scopeControllerGateway: ScopeControllerGateway,
    private val userFactory: UserFactory,
    private val scopeTokenFactory: ScopeTokenFactory,
    private val scopeTokenStorage: ScopeTokenStorage
) : CoroutineTestHelper() {

    @Test
    fun `POST scope, status = 201`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val request = MockCreateScopeTokenRequestFactory.create()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:create")
        )

        val response = scopeControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(request.name, responseScopeToken.name?.orElse(null))
        assertEquals(request.description, responseScopeToken.description?.orElse(null))
        assertFalse(responseScopeToken.system?.orElse(null) ?: true)
        assertNotNull(responseScopeToken.createdAt?.orElse(null))
        assertNotNull(responseScopeToken.updatedAt?.orElse(null))
    }

    @Test
    fun `POST scope, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val request = MockCreateScopeTokenRequestFactory.create()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:create")
        )

        val response = scopeControllerGateway.create(request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET scope, status = 200`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
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
        assertEquals(scopeToken.id, responseScopeToken.id?.orElse(null))
        assertEquals(scopeToken.name, responseScopeToken.name?.orElse(null))
        assertNotNull(responseScopeToken.createdAt?.orElse(null))
        assertNotNull(responseScopeToken.updatedAt?.orElse(null))
    }

    @Test
    fun `GET scope, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:read")
        )

        val response = scopeControllerGateway.read(scopeToken.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(scopeToken.id, responseScopeToken.id?.orElse(null))
        assertEquals(scopeToken.name, responseScopeToken.name?.orElse(null))
        assertNotNull(responseScopeToken.createdAt?.orElse(null))
        assertNotNull(responseScopeToken.updatedAt?.orElse(null))
    }

    @Test
    fun `GET scope_{scope-id}, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update")
        )

        val updatedName = MockScopeNameFactory.create()
        val request = UpdateScopeTokenRequest(
            name = Optional.of(updatedName)
        )
        val response = scopeControllerGateway.update(scopeToken.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseScopeToken = response.responseBody.awaitSingle()

        assertEquals(scopeToken.id, responseScopeToken.id?.orElse(null))
        assertEquals(updatedName, responseScopeToken.name?.orElse(null))
        assertNotNull(responseScopeToken.createdAt?.orElse(null))
        assertNotNull(responseScopeToken.updatedAt?.orElse(null))
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:update")
        )

        val updatedName = MockScopeNameFactory.create()
        val request = UpdateScopeTokenRequest(
            name = Optional.of(updatedName)
        )
        val response = scopeControllerGateway.update(scopeToken.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELETE scope_{scope-id}, status = 204`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:delete")
        )

        val response = scopeControllerGateway.delete(scopeToken.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        assertEquals(null, scopeTokenStorage.load(scopeToken.id))
    }

    @Test
    fun `DELETE scope_{scope-id}, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val name = MockScopeNameFactory.create()
        val scopeToken = scopeTokenFactory.upsert(name)

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("scope:delete")
        )

        val response = scopeControllerGateway.delete(scopeToken.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 200, when add child`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert(MockScopeNameFactory.create(10, "pack"))
        val child = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update", "scope.children:create")
        )

        val finalScope = parent.children().toSet().toMutableSet().also { it.add(child) }
        val request = UpdateScopeTokenRequest(children = Optional.of(finalScope.map { it.id }))
        val response = scopeControllerGateway.update(parent.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(parent.has(child))
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 403, when add child`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert(MockScopeNameFactory.create(10, "pack"))
        val child = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update"),
            pop = listOf("scope.children:create")
        )

        val finalScope = parent.children().toSet().toMutableSet().also { it.add(child) }
        val request = UpdateScopeTokenRequest(children = Optional.of(finalScope.map { it.id }))
        val response = scopeControllerGateway.update(parent.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 200, when delete child`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert(MockScopeNameFactory.create(10, "pack"))
        val child = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update", "scope.children:delete")
        )

        val finalScope = parent.children().toSet().toMutableSet().also { it.remove(child) }
        val request = UpdateScopeTokenRequest(children = Optional.of(finalScope.map { it.id }))
        val response = scopeControllerGateway.update(parent.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertFalse(parent.has(child))
    }

    @Test
    fun `PATCH scope_{scope-id}, status = 403, when remove child`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val parent = scopeTokenFactory.upsert(MockScopeNameFactory.create(10, "pack"))
        val child = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        parent.grant(child)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("scope:update"),
            pop = listOf("scope.children:delete")
        )

        val finalScope = parent.children().toSet().toMutableSet().also { it.remove(child) }
        val request = UpdateScopeTokenRequest(children = Optional.of(finalScope.map { it.id }))
        val response = scopeControllerGateway.update(parent.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
