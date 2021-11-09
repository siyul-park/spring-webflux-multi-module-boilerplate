package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class AuthenticatorManager {
    private val authenticators = mutableListOf<Pair<AuthenticateFilter, Authenticator<*, *>>>()

    fun register(filter: AuthenticateFilter, authenticator: Authenticator<*, *>): AuthenticatorManager {
        authenticators.add(filter to authenticator)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val authenticator = authenticators.find { (filter, _) -> filter.isSubscribe(payload) }?.second
            ?: throw UnsupportedAuthorizationTypeException()
        authenticator as Authenticator<PAYLOAD, *>
        return authenticator.authenticate(payload)
    }
}
