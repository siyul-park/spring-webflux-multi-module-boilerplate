package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.domain.authentication.Authenticator
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.InternalAuthenticationServiceException
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
                val principal = authenticator.authenticate(payload)

                AuthenticationAdapter(principal, credentials)
            } catch (exception: Exception) {
                authentication.isAuthenticated = false
                throw InternalAuthenticationServiceException(exception.message ?: "", exception)
            }
        }
    }
}
