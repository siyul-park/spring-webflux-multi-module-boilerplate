package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.dummy.DummyCreateClientPayload
import io.github.siyual_park.application.server.dummy.DummyCreateUserPayload
import io.github.siyual_park.application.server.gateway.AuthControllerGateway
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.spring.test.CoroutineTest
import io.github.siyual_park.spring.test.IntegrationTest
import io.github.siyual_park.user.domain.UserFactory
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@IntegrationTest
class AuthControllerTest @Autowired constructor(
    private val authControllerGateway: AuthControllerGateway,
    private val userFactory: UserFactory,
    private val clientFactory: ClientFactory,
) : CoroutineTest() {

    @Test
    fun `POST token, status = 201, when grant_type = password`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val createUserPayload = DummyCreateUserPayload.create()
            .also { userFactory.create(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.name,
                password = createUserPayload.password,
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.CREATED, tokenResponse.status)

        val tokens = tokenResponse.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNotNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 401, when grant_type = password`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val createUserPayload = DummyCreateUserPayload.create()
            .also { userFactory.create(it) }

        val otherUserRequest = DummyCreateUserPayload.create()

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.name,
                password = otherUserRequest.password,
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, tokenResponse.status)
    }

    @Test
    fun `POST token, status = 400, when grant_type = password`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val createUserPayload = DummyCreateUserPayload.create()
            .also { userFactory.create(it) }

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.name,
                clientId = client.id
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                password = createUserPayload.password,
                clientId = client.id
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                clientId = client.id
            )
        )
        val case4 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                refreshToken = "dummy_token",
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
        assertEquals(HttpStatus.BAD_REQUEST, case4.status)
    }

    @Test
    fun `POST token, status = 201, when grant_type = client_credentials`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.CLIENT_CREDENTIALS,
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.CREATED, tokenResponse.status)

        val tokens = tokenResponse.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 201, when grant_type = refresh_token`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val createUserPayload = DummyCreateUserPayload.create()
            .also { userFactory.create(it) }

        val tokensByPassword = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.name,
                password = createUserPayload.password,
                clientId = client.id
            )
        ).responseBody.awaitSingle()

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = tokensByPassword.refreshToken,
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.CREATED, response.status)

        val tokens = response.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 401, when grant_type = refresh_token`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = "invalid_token",
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun `POST token, status = 400, when grant_type = refresh_token`() = blocking {
        val client = DummyCreateClientPayload.create()
            .let { clientFactory.create(it) }
        val createUserPayload = DummyCreateUserPayload.create()
            .also { userFactory.create(it) }

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                username = createUserPayload.name,
                clientId = client.id
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                password = createUserPayload.password,
                clientId = client.id
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
    }
}
