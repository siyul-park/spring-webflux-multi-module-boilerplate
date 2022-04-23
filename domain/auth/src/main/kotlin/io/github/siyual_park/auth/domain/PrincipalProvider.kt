package io.github.siyual_park.auth.domain

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Component
class PrincipalProvider {
    suspend fun get(): Principal? {
        val context = ReactiveSecurityContextHolder
            .getContext()
            .awaitSingleOrNull()

        return context?.authentication?.principal as? Principal?
    }
}
