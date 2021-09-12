package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.hasScope
import io.github.siyual_park.auth.repository.ScopeTokenRepository
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class ScopeEvaluator(
    private val scopeTokenRepository: ScopeTokenRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any): Boolean {
        return runBlocking {
            val principal = targetDomainObject ?: authentication.principal
            if (principal !is Principal) {
                return@runBlocking false
            }

            try {
                val scope = when (permission) {
                    is String -> {
                        setOf(scopeTokenRepository.findByNameOrFail(permission))
                    }
                    is Collection<*> -> {
                        scopeTokenRepository.findAllByName(permission.map { it.toString() }).toSet()
                    }
                    else -> {
                        return@runBlocking false
                    }
                }

                return@runBlocking principal.hasScope(scope)
            } catch (e: Exception) {
                return@runBlocking false
            }
        }
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable?,
        targetType: String?,
        permission: Any?
    ): Boolean {
        return false
    }
}
