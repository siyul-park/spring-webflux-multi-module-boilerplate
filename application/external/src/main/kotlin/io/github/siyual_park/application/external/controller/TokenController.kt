package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.configuration.PreDefinedScopeToken
import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.dto.response.CreateTokenResponse
import io.github.siyual_park.application.external.exception.ForbiddenException
import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.PasswordGrantPayload
import io.github.siyual_park.auth.domain.token.TokenExchanger
import io.github.siyual_park.auth.entity.names
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@Api("auth")
@RestController
@RequestMapping("")
class TokenController(
    private val authenticatorManager: AuthenticatorManager,
    private val tokenExchanger: TokenExchanger,
    @Value("\${application.auth.access-token.age}")
    private val accessTokenAge: Long = Duration.ofHours(1).seconds,
    @Value("\${application.auth.refresh-token.age}")
    private val refreshTokenAge: Long = Duration.ofDays(30).seconds
) {

    @PostMapping("/tokens", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(request: CreateTokenRequest): CreateTokenResponse {
        val payload = PasswordGrantPayload(request.username, request.password)

        val principal = authenticatorManager.authenticate(payload)
        if (!principal.scope.names().contains(PreDefinedScopeToken.createAccessToken.name)) {
            throw ForbiddenException()
        }

        val accessToken = tokenExchanger.encoding(
            principal,
            Duration.ofSeconds(accessTokenAge)
        )
        val refreshToken = tokenExchanger.encoding(
            principal,
            Duration.ofSeconds(refreshTokenAge),
            scope = listOf(PreDefinedScopeToken.createAccessToken)
        )

        return CreateTokenResponse(
            accessToken = accessToken,
            tokenType = "bearer",
            expiresIn = accessTokenAge,
            refreshToken = refreshToken
        )
    }
}
