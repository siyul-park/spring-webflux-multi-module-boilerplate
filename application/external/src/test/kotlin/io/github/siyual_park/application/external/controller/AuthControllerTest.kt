package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.GrantType
import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.factory.CreateClientPayloadFactory
import io.github.siyual_park.application.external.factory.CreateUserPayloadFactory
import io.github.siyual_park.application.external.gateway.AuthControllerGateway
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
    private val createUserPayloadFactory = CreateUserPayloadFactory()
    private val createClientPayloadFactory = CreateClientPayloadFactory()

    @Test
    fun testCreateGrantTypePasswordSuccess() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val createUserPayload = createUserPayloadFactory.create()
            .also { userFactory.create(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = createUserPayload.password,
                clientId = client.id!!
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
    fun testCreateGrantTypePasswordFailByIncorrectPassword() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val createUserPayload = createUserPayloadFactory.create()
            .also { userFactory.create(it) }

        val otherUserRequest = createUserPayloadFactory.create()

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = otherUserRequest.password,
                clientId = client.id!!
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, tokenResponse.status)
    }

    @Test
    fun testCreateGrantTypePasswordFailByInvalidRequest() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val createUserPayload = createUserPayloadFactory.create()
            .also { userFactory.create(it) }

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                clientId = client.id!!
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                password = createUserPayload.password,
                clientId = client.id!!
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                clientId = client.id!!
            )
        )
        val case4 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                refreshToken = "dummy_token",
                clientId = client.id!!
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
        assertEquals(HttpStatus.BAD_REQUEST, case4.status)
    }

    @Test
    fun testCreateGrantTypeClientCredentialsSuccess() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.CLIENT_CREDENTIALS,
                clientId = client.id!!
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
    fun testCreateGrantTypeRefreshTokenSuccessByUser() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val createUserPayload = createUserPayloadFactory.create()
            .also { userFactory.create(it) }

        val tokensByPassword = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = createUserPayload.password,
                clientId = client.id!!
            )
        ).responseBody.awaitSingle()

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = tokensByPassword.refreshToken,
                clientId = client.id!!
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
    fun testCreateGrantTypeRefreshTokenFailInvalidToken() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = "invalid_token",
                clientId = client.id!!
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun testCreateGrantTypeRefreshTokenFailInvalidRequest() = blocking {
        val client = createClientPayloadFactory.create()
            .let { clientFactory.create(it) }
        val createUserPayload = createUserPayloadFactory.create()
            .also { userFactory.create(it) }

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                username = createUserPayload.username,
                clientId = client.id!!
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                password = createUserPayload.password,
                clientId = client.id!!
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                clientId = client.id!!
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
    }
}
