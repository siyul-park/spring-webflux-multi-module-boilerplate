package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.UpdateUserContactRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyEmailFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserContactControllerGateway
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.user.domain.UserFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.Optional

@IntegrationTest
class UserContactControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val userContactControllerGateway: UserContactControllerGateway,
    private val userFactory: UserFactory,
) : CoroutineTest() {
    @Test
    fun `GET users_{self-id}_contact, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].contact:read")
        )

        val response = userContactControllerGateway.read(user.id)

        assertEquals(HttpStatus.OK, response.status)
    }

    @Test
    fun `GET users_{self-id}_contact, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].contact:read", "users.contact:read")
        )

        val response = userContactControllerGateway.read(user.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET users_{user-id}_contact, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.contact:read")
        )

        val response = userContactControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.OK, response.status)

        response.responseBody.awaitSingle()
    }

    @Test
    fun `GET users_{user-id}_contact, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].contact:read", "users.contact:read")
        )

        val response = userContactControllerGateway.read(otherUser.id)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{self-id}_contact, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].contact:update")
        )

        val email = DummyEmailFactory.create(10)
        val request = UpdateUserContactRequest(
            email = Optional.of(email)
        )
        val response = userContactControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val contactInfo = response.responseBody.awaitSingle()

        assertEquals(email, contactInfo.email)
    }

    @Test
    fun `PATCH users_{self-id}_contact, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].contact:update", "users.contact:update")
        )

        val email = DummyEmailFactory.create(10)
        val request = UpdateUserContactRequest(
            email = Optional.of(email)
        )
        val response = userContactControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}_contact, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.contact:update")
        )

        val email = DummyEmailFactory.create(10)
        val request = UpdateUserContactRequest(
            email = Optional.of(email)
        )
        val response = userContactControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)

        val contactInfo = response.responseBody.awaitSingle()

        assertEquals(email, contactInfo.email)
    }

    @Test
    fun `PATCH users_{user-id}_contact, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].contact:update", "users.contact:update")
        )

        val email = DummyEmailFactory.create(10)
        val request = UpdateUserContactRequest(
            email = Optional.of(email)
        )
        val response = userContactControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
