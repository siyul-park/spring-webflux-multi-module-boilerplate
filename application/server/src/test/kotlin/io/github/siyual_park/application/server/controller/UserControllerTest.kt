package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserRequest
import io.github.siyual_park.application.server.dummy.RandomNameFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserControllerGateway
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.user.domain.UserFactory
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
class UserControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val userControllerGateway: UserControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
) : CoroutineTest() {

    @Test
    fun `POST users, status = 201`() = blocking {
        val principal = DummyCreateClientPayload.create()
            .let { clientFactory.create(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:create")
        )

        val request = DummyCreateUserRequest.create()
        val response = userControllerGateway.create(request)

        assertEquals(HttpStatus.CREATED, response.status)

        val user = response.responseBody.awaitSingle()

        assertNotNull(user.id)
        assertEquals(request.name, user.name)
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
    fun `GET users, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:read")
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
            push = listOf("users[self]:read")
        )

        val response = userControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(user.name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
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
    fun `PATCH users_self, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:update")
        )

        val name = RandomNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.updateSelf(request)

        assertEquals(HttpStatus.OK, response.status)

        val responseUser = response.responseBody.awaitSingle()

        assertEquals(user.id, responseUser.id)
        assertEquals(name, responseUser.name)
        assertNotNull(responseUser.createdAt)
        assertNotNull(responseUser.updatedAt)
    }

    @Test
    fun `PATCH users_self, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:update")
        )

        val request = UpdateUserRequest(
            name = Optional.of(RandomNameFactory.create(10))
        )
        val response = userControllerGateway.updateSelf(request)

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

        val name = RandomNameFactory.create(10)
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
    fun `PATCH users_{self-id}, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:update", "users:update")
        )
        val name = RandomNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name)
        )
        val response = userControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELEATE users_self, status = 204`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self]:delete")
        )

        val response = userControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)

        val scope = user.getScope().toSet()
        assertEquals(0, scope.size)
    }

    @Test
    fun `DELEATE users_self, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self]:delete", "users:delete")
        )

        val response = userControllerGateway.deleteSelf()

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
    fun `PATCH users_{user-id}, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users:update")
        )

        val name = RandomNameFactory.create(10)
        val request = UpdateUserRequest(
            name = Optional.of(name)
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
            name = Optional.of(RandomNameFactory.create(10))
        )
        val response = userControllerGateway.update(otherUser.id, request)

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

    @Test
    fun `GET users_{self-id}_scope, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].scope:read")
        )

        listOf(false, true).forEach { deep ->
            val response = userControllerGateway.readSelfScope(deep = deep)

            assertEquals(HttpStatus.OK, response.status)

            val responseScope = response.responseBody.asFlow().toList().sortedBy { it.id }
            val scope = user.getScope(deep = deep).toList().sortedBy { it.id }

            assertEquals(scope.map { it.id }, responseScope.map { it.id })
        }
    }

    @Test
    fun `GET users_{self-id}_scope, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].scope:read")
        )

        val response = userControllerGateway.readSelfScope()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_{user-id}_scope, status = 200`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.scope:read")
        )

        listOf(false, true).forEach { deep ->
            val response = userControllerGateway.readScope(otherUser.id, deep = deep)

            assertEquals(HttpStatus.OK, response.status)

            val responseScope = response.responseBody.asFlow().toList().sortedBy { it.id }
            val scope = otherUser.getScope(deep = deep).toList().sortedBy { it.id }

            assertEquals(scope.map { it.id }, responseScope.map { it.id })
        }
    }

    @Test
    fun `GET users_{user-id}_scope, status = 403`() = blocking {
        val principal = DummyCreateUserPayload.create()
            .let { userFactory.create(it).toPrincipal() }

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users.scope:read")
        )

        val response = userControllerGateway.readScope(otherUser.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
