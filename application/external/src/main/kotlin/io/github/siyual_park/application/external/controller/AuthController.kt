package io.github.siyual_park.application.external.controller

import io.github.siyual_park.application.external.dto.GrantType
import io.github.siyual_park.application.external.dto.request.CreateTokenRequest
import io.github.siyual_park.application.external.dto.response.CreateTokenResponse
import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.AuthorizationPayload
import io.github.siyual_park.auth.domain.authenticator.PasswordGrantPayload
import io.github.siyual_park.auth.domain.token.TokenIssuer
import io.github.siyual_park.json.bind.RequestForm
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api("auth")
@RestController
@RequestMapping("")
class AuthController(
    private val authenticatorManager: AuthenticatorManager,
    private val tokenIssuer: TokenIssuer,
    private val mapperManager: MapperManager
) {

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createToken(@Valid @RequestForm request: CreateTokenRequest): CreateTokenResponse {
        val payload = when (request.grantType) {
            GrantType.PASSWORD -> PasswordGrantPayload(request.username!!, request.password!!)
            GrantType.REFRESH_TOKEN -> AuthorizationPayload("bearer", request.refreshToken!!)
        }

        val principal = authenticatorManager.authenticate(payload)
        val tokens = tokenIssuer.issue(principal)

        return mapperManager.map(tokens)
    }
}
