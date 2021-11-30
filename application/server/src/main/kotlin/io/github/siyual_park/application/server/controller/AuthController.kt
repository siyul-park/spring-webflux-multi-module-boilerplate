package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.dto.response.TokenInfo
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.token.TokenIssuer
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.github.siyual_park.client.domain.auth.ClientCredentialsGrantPayload
import io.github.siyual_park.json.bind.RequestForm
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.user.domain.auth.PasswordGrantPayload
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["auth"])
@RestController
@RequestMapping("")
class AuthController(
    private val authenticator: Authenticator,
    private val authorizator: Authorizator,
    private val tokenIssuer: TokenIssuer,
    private val principalRefresher: PrincipalRefresher,
    private val mapperManager: MapperManager
) {

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createToken(@Valid @RequestForm request: CreateTokenRequest): TokenInfo {
        authenticator.authenticate(ClientCredentialsGrantPayload(request.clientId, request.clientSecret))
            .also { if (!authorizator.authorize(it, "token:create")) {
                throw RequiredPermissionException()
            } }

        var principal = authenticator.authenticate(
            when (request.grantType) {
                GrantType.PASSWORD -> PasswordGrantPayload(request.username!!, request.password!!, request.clientId)
                GrantType.CLIENT_CREDENTIALS -> ClientCredentialsGrantPayload(request.clientId, request.clientSecret)
                GrantType.REFRESH_TOKEN -> AuthorizationPayload("bearer", request.refreshToken!!)
            }
        )

        if (request.grantType == GrantType.REFRESH_TOKEN) {
            principal = principalRefresher.refresh(principal)
        }

        val tokens = tokenIssuer.issue(principal)

        return mapperManager.map(tokens)
    }
}
