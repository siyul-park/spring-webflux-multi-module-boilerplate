package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.dto.response.PrincipalInfo
import io.github.siyual_park.application.server.dto.response.TokenInfo
import io.github.siyual_park.application.server.property.TokensProperty
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.domain.token.TokenFactory
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.github.siyual_park.client.domain.auth.ClientCredentialsGrantPayload
import io.github.siyual_park.json.bind.RequestForm
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.AsyncLazy
import io.github.siyual_park.user.domain.auth.PasswordGrantPayload
import io.swagger.annotations.Api
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
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
    private val tokenFactory: TokenFactory,
    private val principalRefresher: PrincipalRefresher,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val tokensProperty: TokensProperty,
    private val mapperContext: MapperContext,
) {
    private val tokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("token:create")
    }
    private val accessTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("access-token:create")
    }
    private val refreshTokenScope = AsyncLazy {
        scopeTokenStorage.loadOrFail("refresh-token:create")
    }

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createToken(@Valid @RequestForm request: CreateTokenRequest): TokenInfo {
        authenticator.authenticate(ClientCredentialsGrantPayload(request.clientId, request.clientSecret))
            .also {
                if (!authorizator.authorize(it, tokenScope.get())) {
                    throw RequiredPermissionException()
                }
            }

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

        val scope = request.scope?.split(" ")
            ?.let { scopeTokenStorage.load(it) }
            ?.toSet()

        if (!authorizator.authorize(principal, accessTokenScope.get())) {
            throw RequiredPermissionException()
        }

        val accessToken = tokenFactory.create(
            principal,
            tokensProperty.accessToken.age,
            pop = setOf(accessTokenScope.get(), refreshTokenScope.get()),
            filter = scope
        )
        val refreshToken = if (authorizator.authorize(principal, refreshTokenScope.get())) {
            tokenFactory.create(
                principal,
                tokensProperty.refreshToken.age,
                pop = setOf(refreshTokenScope.get()),
                filter = scope
            )
        } else {
            null
        }

        return TokenInfo(
            accessToken = accessToken.signature,
            tokenType = "bearer",
            expiresIn = tokensProperty.accessToken.age,
            refreshToken = refreshToken?.signature
        )
    }

    @GetMapping("/principal")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'principal[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: Principal): PrincipalInfo {
        return mapperContext.map(principal)
    }
}
