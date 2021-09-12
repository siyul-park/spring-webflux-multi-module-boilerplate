package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.GrantType
import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.factory.CreateUserRequestFactory
import io.github.siyual_park.application.external.gateway.AuthControllerGateway
import io.github.siyual_park.application.external.gateway.UserControllerGateway
import io.github.siyual_park.spring.test.ControllerTest
import io.github.siyual_park.spring.test.CoroutineTest
import kotlinx.coroutines.reactive.awaitSingle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

@ControllerTest
class AuthControllerTest @Autowired constructor(
    private val userControllerGateway: UserControllerGateway,
    private val authControllerGateway: AuthControllerGateway
) : CoroutineTest() {
    private val createUserRequestFactory = CreateUserRequestFactory()

    @Test
    fun testCreateGrantTypePasswordSuccess() = blocking {
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val tokenResponse = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserRequest.name,
                password = createUserRequest.password
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
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val otherUserRequest = createUserRequestFactory.create()

        val tokenResponse = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserRequest.name,
                password = otherUserRequest.password
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, tokenResponse.status)
    }

    @Test
    fun testCreateGrantTypePasswordFailByInvalidRequest() = blocking {
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val case1 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserRequest.name,
            )
        )
        val case2 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                password = createUserRequest.password,
            )
        )
        val case3 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
            )
        )
        val case4 = authControllerGateway.create(
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
    fun testCreateGrantTypeRefreshTokenSuccess() = blocking {
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val tokensByPassword = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.PASSWORD,
                username = createUserRequest.name,
                password = createUserRequest.password
            )
        ).responseBody.awaitSingle()

        val response = authControllerGateway.create(
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
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val response = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                refreshToken = "invalid_token"
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun testCreateGrantTypeRefreshTokenFailInvalidRequest() = blocking {
        val createUserRequest = createUserRequestFactory.create()
        userControllerGateway.create(createUserRequest)

        val case1 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                username = createUserRequest.name,
            )
        )
        val case2 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
                password = createUserRequest.password,
            )
        )
        val case3 = authControllerGateway.create(
            CreateTokenRequest(
                grantType = GrantType.REFRESH_TOKEN,
            )
        )

        assertEquals(HttpStatus.BAD_REQUEST, case1.status)
        assertEquals(HttpStatus.BAD_REQUEST, case2.status)
        assertEquals(HttpStatus.BAD_REQUEST, case3.status)
    }
}
