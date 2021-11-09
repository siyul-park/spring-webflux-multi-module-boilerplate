package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authenticator {
    private val authenticators = mutableListOf<Pair<AuthenticateFilter, AuthenticateProcessor<*, *>>>()

    fun register(filter: AuthenticateFilter, processor: AuthenticateProcessor<*, *>): Authenticator {
        authenticators.add(filter to processor)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val authenticator = authenticators.find { (filter, _) -> filter.isSubscribe(payload) }?.second
            ?: throw UnsupportedAuthorizationTypeException()
        authenticator as AuthenticateProcessor<PAYLOAD, *>
        return authenticator.authenticate(payload)
    }
}
