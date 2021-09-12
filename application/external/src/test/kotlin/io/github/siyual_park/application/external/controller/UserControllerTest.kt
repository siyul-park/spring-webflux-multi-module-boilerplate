package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.factory.CreateUserRequestFactory
import io.github.siyual_park.application.external.gateway.UserControllerGateway
import io.github.siyual_park.spring.test.ControllerTest
import io.github.siyual_park.spring.test.CoroutineTest
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@ControllerTest
class UserControllerTest @Autowired constructor(
    private val userControllerGateway: UserControllerGateway
) : CoroutineTest() {
    private val createUserRequestFactory = CreateUserRequestFactory()

    @Test
    fun testCreateSuccess() = blocking {
        val request = createUserRequestFactory.create()
        val response = userControllerGateway.create(request)

        assertEquals(response.status, HttpStatus.CREATED)

        val body = response.responseBody.awaitSingle()

        assertNotNull(body.id)
        assertEquals(request.name, body.name)
        assertNotNull(body.createdAt)
        assertNotNull(body.updatedAt)
    }

    @Test
    fun testCreateFail() = blocking {
        val request = createUserRequestFactory.create()
        userControllerGateway.create(request)

        val response = userControllerGateway.create(request)
        assertEquals(response.status, HttpStatus.CONFLICT)
    }
}
