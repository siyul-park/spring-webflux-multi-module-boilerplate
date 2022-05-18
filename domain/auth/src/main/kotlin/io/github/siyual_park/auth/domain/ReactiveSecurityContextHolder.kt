package io.github.siyual_park.auth.domain

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder

suspend fun getPrincipal(): Principal? {
    val context = ReactiveSecurityContextHolder
        .getContext()
        .awaitSingleOrNull()

    return context?.authentication?.principal as? Principal?
}
