package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.GrantType
import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.factory.CreateUserPayloadFactory
import io.github.siyual_park.application.external.gateway.AuthControllerGateway
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
    private val userFactory: UserFactory
) : CoroutineTest() {
    private val createUserPayloadFactory = CreateUserPayloadFactory()

    @Test
    fun testCreateGrantTypePasswordSuccess() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = createUserPayload.password
            )
        )

        assertEquals(HttpStatus.CREATED, tokenResponse.status)

        val tokens = tokenResponse.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn > 0L)
        assertNotNull(tokens.refreshToken)
    }

    @Test
    fun testCreateGrantTypePasswordFailByIncorrectPassword() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val otherUserRequest = createUserPayloadFactory.create()

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = otherUserRequest.password
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, tokenResponse.status)
    }

    @Test
    fun testCreateGrantTypePasswordFailByInvalidRequest() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                password = createUserPayload.password,
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
            )
        )
        val case4 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                refreshToken = "dummy_token"
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
        assertEquals(HttpStatus.BAD_REQUEST, case4.status)
    }

    @Test
    fun testCreateGrantTypeRefreshTokenSuccessByUser() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val tokensByPassword = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserPayload.username,
                password = createUserPayload.password
            )
        ).responseBody.awaitSingle()

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = tokensByPassword.refreshToken
            )
        )

        assertEquals(HttpStatus.CREATED, response.status)

        val tokens = response.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn > 0L)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun testCreateGrantTypeRefreshTokenFailInvalidToken() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val response = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = "invalid_token"
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun testCreateGrantTypeRefreshTokenFailInvalidRequest() = blocking {
        val createUserPayload = createUserPayloadFactory.create()
        userFactory.create(createUserPayload)

        val case1 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                username = createUserPayload.username,
            )
        )
        val case2 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                password = createUserPayload.password,
            )
        )
        val case3 = authControllerGateway.createToken(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
    }
}
