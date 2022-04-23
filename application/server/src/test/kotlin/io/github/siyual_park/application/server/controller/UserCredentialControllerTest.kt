package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.UpdateUserCredentialRequest
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.dummy.DummyStringFactory
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.application.server.gateway.UserCredentialControllerGateway
import io.github.siyual_park.coroutine.test.CoroutineTest
import io.github.siyual_park.user.domain.UserFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.Optional

@IntegrationTest
class UserCredentialControllerTest @Autowired constructor(
    private val gatewayAuthorization: GatewayAuthorization,
    private val userCredentialControllerGateway: UserCredentialControllerGateway,
    private val userFactory: UserFactory,
) : CoroutineTest() {
    @Test
    fun `PATCH users_{self-id}_credential, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users[self].credential:update")
        )

        val password = DummyStringFactory.create(10)
        val request = UpdateUserCredentialRequest(
            password = Optional.of(password)
        )
        val response = userCredentialControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.OK, response.status)

        response.responseBody.awaitSingle()
    }

    @Test
    fun `PATCH users_{self-id}_credential, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].credential:update", "users.credential:update")
        )
        val password = DummyStringFactory.create(10)
        val request = UpdateUserCredentialRequest(
            password = Optional.of(password)
        )
        val response = userCredentialControllerGateway.update(user.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `PATCH users_{user-id}_credential, status = 200`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("users.credential:update")
        )

        val password = DummyStringFactory.create(10)
        val request = UpdateUserCredentialRequest(
            password = Optional.of(password)
        )
        val response = userCredentialControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.OK, response.status)

        response.responseBody.awaitSingle()
    }

    @Test
    fun `PATCH users_{user-id}_credential, status = 403`() = blocking {
        val payload = DummyCreateUserPayload.create()
        val user = userFactory.create(payload)
        val principal = user.toPrincipal()

        val otherUser = DummyCreateUserPayload.create()
            .let { userFactory.create(it) }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("users[self].credential:update", "users.credential:update")
        )
        val password = DummyStringFactory.create(10)
        val request = UpdateUserCredentialRequest(
            password = Optional.of(password)
        )
        val response = userCredentialControllerGateway.update(otherUser.id, request)

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }
}
