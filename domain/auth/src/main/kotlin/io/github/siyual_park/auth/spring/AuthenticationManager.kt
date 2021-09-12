package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.domain.authenticator.AuthenticatorManager
import io.github.siyual_park.auth.domain.authenticator.payload.AuthorizationPayload
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val authenticatorManager: AuthenticatorManager
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return mono {
            try {
                val type = authentication.principal
                val credentials = authentication.credentials

                val payload = AuthorizationPayload(type as String, credentials as String)
                val parsedAuthentication = authenticatorManager.authenticate(payload)

                AuthenticationAdapter(parsedAuthentication, credentials)
            } catch (exception: Exception) {
                authentication.isAuthenticated = false
                authentication
            }
        }
    }
}
