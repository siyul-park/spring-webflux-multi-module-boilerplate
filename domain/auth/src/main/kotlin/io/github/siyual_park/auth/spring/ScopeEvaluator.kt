package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.repository.ScopeTokenRepository
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class ScopeEvaluator(
    private val scopeTokenRepository: ScopeTokenRepository
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any, permission: Any): Boolean {
        return runBlocking {
            if (targetDomainObject !is String || permission !is String || authentication !is AuthenticationAdapter) {
                return@runBlocking false
            }

            try {
                val authorities = authentication.authorities
                val scope = scopeTokenRepository.findByNameOrFail("$targetDomainObject:$permission")

                return@runBlocking authorities.any { it.authority == scope.id.toString() }
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
