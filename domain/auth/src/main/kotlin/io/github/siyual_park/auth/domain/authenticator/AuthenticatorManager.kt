package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.exception.AuthenticatorNotExistsException
import org.springframework.stereotype.Component

@Component
class AuthenticatorManager {
    private val authenticators = mutableMapOf<Class<*>, Authenticator<*, *, *>>()

    fun register(authenticator: Authenticator<*, *, *>): AuthenticatorManager {
        authenticators[authenticator.infoClazz.java] = authenticator
        return this
    }

    suspend fun <INFO : AuthenticationInfo> authenticate(info: INFO): Authentication<*> {
        val authenticator = authenticators[info.javaClass] ?: throw AuthenticatorNotExistsException()
        authenticator as Authenticator<INFO, *, *>
        return authenticator.authenticate(info)
    }
}
