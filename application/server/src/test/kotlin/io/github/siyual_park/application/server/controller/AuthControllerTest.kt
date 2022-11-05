package io.github.siyual_park.application.server.controller

import io.github.siyual_park.IntegrationTest
import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.gateway.AuthControllerGateway
import io.github.siyual_park.application.server.gateway.GatewayAuthorization
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.MockCreateClientPayloadFactory
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.MockCreateUserPayloadFactory
import io.github.siyual_park.user.domain.UserStorage
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
    private val gatewayAuthorization: GatewayAuthorization,
    private val authControllerGateway: AuthControllerGateway,
    private val userStorage: UserStorage,
    private val clientStorage: ClientStorage,
) : CoroutineTestHelper() {

    @Test
    fun `POST token, status = 201, when grant_type = password`() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val createUserPayload = MockCreateUserPayloadFactory.create()
            .also { userStorage.save(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest.Password(
                username = createUserPayload.name,
                password = createUserPayload.password,
                clientId = client.id,
                clientSecret = client.secret
            )
        )

        assertEquals(HttpStatus.CREATED, tokenResponse.status)

        val tokens = tokenResponse.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("Bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNotNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 201, when grant_type = client_credentials`() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }

        val tokenResponse = authControllerGateway.createToken(
            CreateTokenRequest.ClientCredentials(
                clientId = client.id,
                clientSecret = client.secret
            )
        )

        assertEquals(HttpStatus.CREATED, tokenResponse.status)

        val tokens = tokenResponse.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("Bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 201, when grant_type = refresh_token`() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }
        val createUserPayload = MockCreateUserPayloadFactory.create()
            .also { userStorage.save(it) }

        val tokensByPassword = authControllerGateway.createToken(
            CreateTokenRequest.Password(
                username = createUserPayload.name,
                password = createUserPayload.password,
                clientId = client.id,
                clientSecret = client.secret
            )
        ).responseBody.awaitSingle()

        val response = authControllerGateway.createToken(
            CreateTokenRequest.RefreshToken(
                refreshToken = tokensByPassword.refreshToken!!,
                clientId = client.id,
                clientSecret = client.secret
            )
        )

        assertEquals(HttpStatus.CREATED, response.status)

        val tokens = response.responseBody.awaitSingle()

        assertTrue(tokens.accessToken.isNotEmpty())
        assertEquals("Bearer", tokens.tokenType)
        assertTrue(tokens.expiresIn.seconds > 0L)
        assertNull(tokens.refreshToken)
    }

    @Test
    fun `POST token, status = 401, when grant_type = refresh_token`() = blocking {
        val client = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it) }

        val response = authControllerGateway.createToken(
            CreateTokenRequest.RefreshToken(
                refreshToken = "invalid_token",
                clientId = client.id
            )
        )

        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun `GET principal, status = 200, when type = client_principal`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("principal[self]:read")
        )

        val response = authControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val principalInfo = response.responseBody.awaitSingle()

        assertEquals(principal.clientId, principalInfo.claims?.orElse(null)?.get("cid")?.let { ULID.fromString(it.toString()) })
        assertEquals("client_principal", principalInfo.type?.orElse(null))
        assertTrue(principalInfo.claims?.orElse(null)?.get("cid") == principal.clientId.toString())
    }

    @Test
    fun `GET principal, status = 403, when type = client_principal`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("principal[self]:read")
        )

        val response = authControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `GET principal, status = 200, when type = user_principal`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("principal[self]:read")
        )

        val response = authControllerGateway.readSelf()

        assertEquals(HttpStatus.OK, response.status)

        val principalInfo = response.responseBody.awaitSingle()

        assertEquals(principal.userId, principalInfo.claims?.orElse(null)?.get("uid")?.let { ULID.fromString(it.toString()) })
        assertEquals(principal.clientId, principalInfo.claims?.orElse(null)?.get("cid")?.let { ULID.fromString(it.toString()) })
        assertEquals("user_principal", principalInfo.type?.orElse(null))
    }

    @Test
    fun `GET principal, status = 403, when type = user_principal`() = blocking {
        val principal = MockCreateUserPayloadFactory.create()
            .let { userStorage.save(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            pop = listOf("principal[self]:read")
        )

        val response = authControllerGateway.readSelf()

        assertEquals(HttpStatus.FORBIDDEN, response.status)
    }

    @Test
    fun `DELETE principal, status = 204`() = blocking {
        val principal = MockCreateClientPayloadFactory.create()
            .let { clientStorage.save(it).toPrincipal() }

        gatewayAuthorization.setPrincipal(
            principal,
            push = listOf("principal[self]:delete")
        )

        val response = authControllerGateway.deleteSelf()

        assertEquals(HttpStatus.NO_CONTENT, response.status)
    }
}
