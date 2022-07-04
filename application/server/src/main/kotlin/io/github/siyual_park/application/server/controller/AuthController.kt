package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.application.server.dto.request.CreateTokenRequest
import io.github.siyual_park.application.server.dto.response.PrincipalInfo
import io.github.siyual_park.application.server.dto.response.TokenInfo
import io.github.siyual_park.application.server.property.TokensProperty
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.RefreshTokenPayload
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.auth.domain.token.Token
import io.github.siyual_park.auth.domain.token.TokenFactoryProvider
import io.github.siyual_park.auth.domain.token.TokenStorage
import io.github.siyual_park.auth.domain.token.TokenTemplate
import io.github.siyual_park.auth.exception.RequiredPermissionException
import io.github.siyual_park.client.domain.auth.ClientCredentialsGrantPayload
import io.github.siyual_park.data.cache.SuspendLazy
import io.github.siyual_park.json.bind.RequestForm
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.presentation.project.ProjectionParserFactory
import io.github.siyual_park.user.domain.auth.PasswordGrantPayload
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Tag(name = "auth")
@RestController
@RequestMapping("")
class AuthController(
    private val authenticator: Authenticator,
    private val authorizator: Authorizator,
    tokenFactoryProvider: TokenFactoryProvider,
    projectionParserFactory: ProjectionParserFactory,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val tokensProperty: TokensProperty,
    private val tokenStorage: TokenStorage,
    private val mapperContext: MapperContext,
) {
    private val projectionParser = projectionParserFactory.create(PrincipalInfo::class)

    private val tokenScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("token:create")
    }
    private val accessTokenScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("access-token:create")
    }
    private val refreshTokenScope = SuspendLazy {
        scopeTokenStorage.loadOrFail("refresh-token:create")
    }

    private val accessTokenFactory = SuspendLazy {
        tokenFactoryProvider.get(
            TokenTemplate(
                type = "acs",
                limit = listOf(
                    "pid" to 1
                ),
                pop = setOf(accessTokenScope.get(), refreshTokenScope.get())
            )
        )
    }
    private val refreshTokenFactory = SuspendLazy {
        tokenFactoryProvider.get(
            TokenTemplate(
                type = "rfr",
                pop = setOf(refreshTokenScope.get()),
            )
        )
    }

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createToken(@Valid @RequestForm request: CreateTokenRequest): TokenInfo {
        val principal = auth(request)
        val (accessToken, refreshToken) = issue(principal, request)

        return TokenInfo(
            accessToken = accessToken.signature,
            tokenType = "Bearer",
            expiresIn = tokensProperty.accessToken.age,
            refreshToken = refreshToken?.signature
        )
    }

    private suspend fun auth(request: CreateTokenRequest): Principal {
        authenticator.authenticate(ClientCredentialsGrantPayload(request.clientId, request.clientSecret))
            .also {
                if (!authorizator.authorize(it, tokenScope.get())) {
                    throw RequiredPermissionException()
                }
            }
        return authenticator.authenticate(
            when (request.grantType) {
                GrantType.PASSWORD -> PasswordGrantPayload(request.username!!, request.password!!, request.clientId)
                GrantType.CLIENT_CREDENTIALS -> ClientCredentialsGrantPayload(request.clientId, request.clientSecret)
                GrantType.REFRESH_TOKEN -> RefreshTokenPayload(request.refreshToken!!)
            }
        )
    }

    private suspend fun issue(principal: Principal, request: CreateTokenRequest): Pair<Token, Token?> {
        if (!authorizator.authorize(principal, accessTokenScope.get())) {
            throw RequiredPermissionException()
        }

        val scope = request.scope?.split(" ")
            ?.let { scopeTokenStorage.load(it) }
            ?.toSet()

        val refreshToken = if (request.grantType == GrantType.REFRESH_TOKEN) {
            tokenStorage.load(request.refreshToken!!)
        } else if (authorizator.authorize(principal, refreshTokenScope.get())) {
            refreshTokenFactory.get().create(
                principal,
                tokensProperty.refreshToken.age,
                filter = setOf(accessTokenScope.get()),
            )
        } else {
            null
        }
        val accessToken = accessTokenFactory.get().create(
            principal,
            tokensProperty.accessToken.age,
            claims = refreshToken?.id?.toString()?.let { mapOf("pid" to it) },
            filter = scope,
        )

        return if (request.grantType != GrantType.REFRESH_TOKEN) {
            accessToken to refreshToken
        } else {
            accessToken to null
        }
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    suspend fun readSelf(
        @AuthenticationPrincipal principal: Principal,
        @RequestParam("fields", required = false) fields: Collection<String>? = null
    ): PrincipalInfo = authorizator.withAuthorize(listOf("principal[self]:read")) {
        mapperContext.map(Projection(principal, projectionParser.parse(fields)))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun deleteSelf(authentication: Authentication) = authorizator.withAuthorize(listOf("principal[self]:delete")) {
        val credential = authentication.credentials.toString()
        val token = tokenStorage.loadOrFail(credential)
        token.clear()
    }
}
