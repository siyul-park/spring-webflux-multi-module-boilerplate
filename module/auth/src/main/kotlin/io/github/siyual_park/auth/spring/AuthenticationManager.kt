package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.domain.authenticator.Authenticator
import io.github.siyual_park.auth.domain.authenticator.AuthorizationPayload
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationManager(
    private val authenticator: Authenticator
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return mono {
            try {
                val type = authentication.principal
                val credentials = authentication.credentials

                val payload = AuthorizationPayload(type as String, credentials as String)
                val parsedAuthentication = authenticator.authenticate(payload)

                AuthenticationAdapter(parsedAuthentication, credentials)
            } catch (exception: Exception) {
                authentication.isAuthenticated = false
                authentication
            }
        }
    }
}
