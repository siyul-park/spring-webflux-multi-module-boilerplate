package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.authenticator.payload.AuthenticationPayload
import io.github.siyual_park.auth.domain.principal.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Component
class AuthenticatorManager {
    private val authenticators = mutableMapOf<Class<*>, Authenticator<*, *>>()

    fun register(authenticator: Authenticator<*, *>): AuthenticatorManager {
        authenticators[authenticator.payloadClazz.java] = authenticator
        return this
    }

    suspend fun <PAYLOAD : AuthenticationPayload> authenticate(payload: PAYLOAD): Principal {
        val authenticator = authenticators[payload.javaClass] ?: throw UnsupportedAuthorizationTypeException()
        authenticator as Authenticator<PAYLOAD, *>
        return authenticator.authenticate(payload)
    }
}
