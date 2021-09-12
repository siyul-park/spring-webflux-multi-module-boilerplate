package io.github.siyual_park.auth.domain.principal

import io.github.siyual_park.auth.domain.ScopeFinder
import io.github.siyual_park.auth.exception.UnsupportedPrincipalException
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class PrincipalRefresher(
    private val scopeFinder: ScopeFinder,
) {
    suspend fun <T : Principal> refresh(principal: T): T {
        return when (principal.javaClass) {
            UserPrincipal::class.java -> {
                val exitedScope = scopeFinder.findByUserId(principal.id.toLong()).toSet()
                UserPrincipal(
                    id = principal.id,
                    scope = principal.scope.filter { exitedScope.contains(it) }.toSet()
                ) as T
            }
            else -> throw UnsupportedPrincipalException()
        }
    }
}
