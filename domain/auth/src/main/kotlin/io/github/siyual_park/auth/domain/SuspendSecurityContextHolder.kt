package io.github.siyual_park.auth.domain

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext

object SuspendSecurityContextHolder {
    suspend fun getPrincipal(): Principal? {
        val context = getContext()
        return context?.authentication?.principal as? Principal?
    }

    suspend fun getContext(): SecurityContext? {
        return ReactiveSecurityContextHolder
            .getContext()
            .awaitSingleOrNull()
    }
}
