package io.github.siyual_park.application.server.controller

import com.github.javafaker.Faker
import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.MockCreateUserRequestFactory
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserControllerGateway
import io.github.siyual_park.auth.domain.scope_token.MockScopeNameFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.util.username
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

    private val faker = Faker()

    @Test
    fun `POST users, status = 201`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:create", "users.contact:read")
        )

        val request = MockCreateUserRequestFactory.create()
        val response = userControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val userInfo = response.responseBody.awaitSingle()

        assertNotNull(userInfo.id)
        assertEquals(request.name, userInfo.name?.orElse(null))
        assertEquals(request.email, userInfo.email?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `POST users, status = 409`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:create")
        )

        val request = MockCreateUserRequestFactory.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.CONFLICT, response.status)
    }

    @Test
    fun `POST users, status = 403`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:create")
        )

        val request = MockCreateUserRequestFactory.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `POST users, status = 400`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:create")
        )

        val request = MockCreateUserRequestFactory.create(
            MockCreateUserRequestFactory.Template(
                name = Optional.of(faker.name().username(25))
            )
        )
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `GET users, status = 200`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
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

        val userInfo = responseUsers[0]
        assertEquals(user.id, userInfo.id?.orElse(null))
        assertEquals(user.name, userInfo.name?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `GET users, status = 403`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
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
    fun `GET users_{self-id}, status = 200`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:read"),
        )

        val response = userControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()

        assertEquals(user.id, userInfo.id?.orElse(null))
        assertEquals(user.name, userInfo.name?.orElse(null))
        assertEquals(user.email, userInfo.email?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `GET users_{self-id}, status = 200, with scope`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].scope:read", "users.read"),
            pop = listOf("users.scope:read")
        )

        val response = userControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()
        val scopeInfo = userInfo.scope?.orElse(null)?.toList()?.sortedBy { it.id?.orElse(null) }
        val scope = user.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, scopeInfo?.map { it.id?.orElse(null) })
    }

    @Test
    fun `GET users_{self-id}, status = 403`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
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
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val name = faker.name().username()
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()

        assertEquals(user.id, userInfo.id?.orElse(null))
        assertEquals(name, userInfo.name?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `PATCH users_{self-id}, status = 400, when name is null`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
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
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val request = UpdateUserRequest(
            name = Optional.of(faker.name().username(25))
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.status)
    }

    @Test
    fun `PATCH users_{self-id}, status = 403`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:update", "users:update")
        )
        val name = faker.name().username()
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE users_{self-id}, status = 204`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
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
        val payload = MockCreateUserPayloadFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:read")
        )

        val response = userControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()

        assertEquals(otherUser.id, userInfo.id?.orElse(null))
        assertEquals(otherUser.name, userInfo.name?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `GET users_{user-id}, status = 200, with scope`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.scope:read", "users.read")
        )

        val response = userControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()
        val scopeInfo = userInfo.scope?.orElse(null)?.toList()?.sortedBy { it.id?.orElse(null) }
        val scope = otherUser.getScope(deep = false).toList().sortedBy { it.id }

        assertEquals(scope.map { it.id }, scopeInfo?.map { it.id?.orElse(null) })
    }

    @Test
    fun `GET users_{user-id}, status = 403`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update")
        )

        val name = faker.name().username()
        val request = UpdateUserRequest(
            name = Optional.of(name),
        )
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val userInfo = response.responseBody.awaitSingle()

        assertEquals(otherUser.id, userInfo.id?.orElse(null))
        assertEquals(name, userInfo.name?.orElse(null))
        assertNotNull(userInfo.createdAt?.orElse(null))
        assertNotNull(userInfo.updatedAt?.orElse(null))
    }

    @Test
    fun `PATCH users_{user-id}, status = 200, with password`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users[self].credential:update")
        )

        val password = faker.internet().password()
        val request = UpdateUserRequest(
            password = Optional.of(password)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(user.getCredential().isPassword(password))
    }

    @Test
    fun `PATCH users_{user-id}, status = 200, with grant scope`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users.scope:create", "users.scope:read")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.add(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))

        val response = userControllerGateway.update(otherUser.id, request)
        val userInfo = response.responseBody.awaitSingle()

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(userInfo.scope?.orElseGet { null }?.map { it.id?.orElseGet { null } }?.contains(scope.id) == true)
        assertTrue(otherUser.has(scope))
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with grant scope`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(faker.name().username())

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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(faker.name().username())

        otherUser.grant(scope)

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update", "users.scope:delete", "users.scope:read")
        )

        val finalScope = otherUser.getScope(false).toSet().toMutableSet().also { it.remove(scope) }
        val request = UpdateUserRequest(scope = Optional.of(finalScope.map { it.id }))

        val response = userControllerGateway.update(otherUser.id, request)
        val userInfo = response.responseBody.awaitSingle()

        assertEquals(HttpStatus.OK, response.status)
        assertTrue(userInfo.scope?.orElseGet { null }?.map { it.id?.orElseGet { null } }?.contains(scope.id) == false)
        assertFalse(otherUser.has(scope))
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with revoke scope`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        val scope = scopeTokenFactory.upsert(MockScopeNameFactory.create())

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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:update")
        )

        val request = UpdateUserRequest(
            name = Optional.of(faker.name().username())
        )
        val response = userControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}, status = 403, with password`() = blocking {
        val payload = MockCreateUserPayloadFactory.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].credential:update", "users.credential:update")
        )
        val password = faker.internet().password()
        val request = UpdateUserRequest(
            password = Optional.of(password)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE users_{user-id}, status = 204`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
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
        val principal = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = MockCreateUserPayloadFactory.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users:delete")
        )

        val response = userControllerGateway.delete(otherUser.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
