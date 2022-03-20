package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class Authenticator {
    private val strategies = mutableListOf<Pair<AuthenticateFilter, AuthenticateStrategy<*, *>>>()

    fun register(filter: AuthenticateFilter, strategy: AuthenticateStrategy<*, *>): Authenticator {
        strategies.add(filter to strategy)
        return this
    }

    suspend fun <PAYLOAD : Any> authenticate(payload: PAYLOAD): Principal {
        val strategies = strategies
            .filter { (filter, _) -> filter.isSubscribe(payload) }
            .map { (_, strategy) -> strategy }

        var exception: RuntimeException? = null
        for (strategy in strategies) {
            strategy as AuthenticateStrategy<PAYLOAD, *>
            try {
                val principal = strategy.authenticate(payload)
                if (principal != null) {
                    return principal
                }
            } catch (e: RuntimeException) {
                exception = e
            }
        }

        throw UnsupportedAuthorizationTypeException(exception?.message)
    }
}
