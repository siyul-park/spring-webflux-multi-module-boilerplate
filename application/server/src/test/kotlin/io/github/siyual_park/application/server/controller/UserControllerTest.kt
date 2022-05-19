package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserRequest
import io.github.siyual_park.application.server.dummy.DummyNameFactory
import io.github.siyual_park.application.server.dummy.DummyStringFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserControllerGateway
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
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
class UserControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val userControllerGateway: UserControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
    private val scopeTokenFactory: ScopeTokenFactory
) : CoroutineTestHelper() {

    @Test
    fun `POST users, status = 201`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:create", "users.contact:read")
        )

        val request = DummyCreateUserRequest.create()
        val response = userControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val user = response.responseBody.awaitSingle()

        assertNotNull(user.id)
        assertEquals(request.name, user.name)
        assertEquals(request.email, user.email)
        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
    }

    @Test
    fun `POST users, status = 409`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:create")
        )

        val request = DummyCreateUserRequest.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun `POST users, status = 403`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:create")
        )

        val request = DummyCreateUserRequest.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `POST users, status = 400`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:create")
        )

        val request = DummyCreateUserRequest.create(
            DummyCreateUserRequest.Template(
                name = Presence.ofNullable(DummyNameFactory.create(25))
            )
        )
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `GET users, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:read"),
        )

        val response = userControllerGateway.readAll(
            name = "eq:${user.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )

        assertEquals(HttpStatus.OK, response.status)

        val responseUsers = response.responseBody.asFlow().toList()
        assertEquals(1, responseUsers.size)

        val responseUser = responseUsers[0]
        assertEquals(user.id, responseUser.id)
        assertEquals(user.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `GET users, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:read")
        )

        val response = userControllerGateway.readAll(
            name = "eq:${user.name}",
            sort = "asc:created_at",
            page = 0,
            perPage = 1
        )
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_self, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:read")
        )

        val response = userControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(user.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `GET users_self, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:read")
        )

        val response = userControllerGateway.readSelf()
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_{self-id}, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:read"),
        )

        val response = userControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(user.name, responseUser.name)
        assertEquals(user.email, responseUser.email)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `GET users_{self-id}, status = 200, with scope`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].scope:read", "users.read"),
            pop = listOf("users.scope:read")
        )

        val response = userControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()
        val responseScope = responseUser.scope?.toList()?.sortedBy { it.id }
        val scope = user.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, responseScope?.map { it.id })
    }

    @Test
    fun `GET users_{self-id}, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:read", "users:read")
        )

        val response = userControllerGateway.read(user.id)
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{self-id}, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val name = DummyNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `PATCH users_{self-id}, status = 400, when name is null`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val request = UpdateUserRequest(
            name = Optional.empty()
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `PATCH users_{self-id}, status = 400, when name is excess size`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val request = UpdateUserRequest(
            name = Optional.of(DummyNameFactory.create(25))
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `PATCH users_{self-id}, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:update", "users:update")
        )
        val name = DummyNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE users_{self-id}, status = 204`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:delete")
        )

        val response = userControllerGateway.delete(user.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val scope = user.getScope().toSet()
        assertEquals(0, scope.size)
    }

    @Test
    fun `DELEATE users_{self-id}, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:delete", "users:delete")
        )

        val response = userControllerGateway.delete(user.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_{user-id}, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:read")
        )

        val response = userControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(otherUser.id, responseUser.id)
        assertEquals(otherUser.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `GET users_{user-id}, status = 200, with scope`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.scope:read", "users.read")
        )

        val response = userControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()
        val responseScope = responseUser.scope?.toList()?.sortedBy { it.id }
        val scope = otherUser.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, responseScope?.map { it.id })
    }

    @Test
    fun `GET users_{user-id}, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:read")
        )

        val response = userControllerGateway.read(otherUser.id)
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_{user-id}, status = 404`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        otherUser.clear()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:read")
        )

        val response = userControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }

    @Test
    fun `PATCH users_{user-id}, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update")
        )

        val name = DummyNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name),
        )
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(otherUser.id, responseUser.id)
        assertEquals(name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `PATCH users_{user-id}, status = 200, with password`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users[self].credential:update")
        )

        val password = DummyStringFactory.create(10)
        val request = UpdateUserRequest(
            password = Optional.of(password)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(user.getCredential().isPassword(password))
    }

    @Test
    fun `PATCH users_{user-id}, status = 200, with grant scope`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users.scope:create")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.add(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(otherUser.has(scope))
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with grant scope`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update"),
            pop = listOf("users.scope:create")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.add(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}, status = 200, with revoke scope`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        otherUser.grant(scope)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users.scope:delete")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.remove(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertFalse(otherUser.has(scope))
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with revoke scope`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(DummyNameFactory.create(10))

        otherUser.grant(scope)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update"),
            pop = listOf("users.scope:delete")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.remove(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:update")
        )

        val request = UpdateUserRequest(
            name = Optional.of(DummyNameFactory.create(10))
        )
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with password`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].credential:update", "users.credential:update")
        )
        val password = DummyStringFactory.create(10)
        val request = UpdateUserRequest(
            password = Optional.of(password)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE users_{user-id}, status = 204`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:delete")
        )

        val response = userControllerGateway.delete(otherUser.id)

        assertEquals(HttpStatus.NO_CONTENT, response.status)
    }

    @Test
    fun `DELEATE users_{user-id}, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:delete")
        )

        val response = userControllerGateway.delete(otherUser.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
