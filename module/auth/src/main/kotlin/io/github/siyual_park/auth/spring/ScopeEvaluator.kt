package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeEvaluator
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class ScopeEvaluator(
    private val scopeTokenFinder: ScopeTokenFinder,
    private val scopeEvaluator: ScopeEvaluator,
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any): Boolean {
        return runBlocking {
            val principal = authentication.principal
            if (principal !is Principal) {
                return@runBlocking false
            }

            try {
                val scope = when (permission) {
                    is String -> {
                        setOf(scopeTokenFinder.findByNameOrFail(permission, cache = true))
                    }
                    is Collection<*> -> {
                        scopeTokenFinder.findAllByName(permission.map { it.toString() }, cache = true).toSet()
                    }
                    else -> {
                        return@runBlocking false
                    }
                }

                val adjustedTargetDomainObject = if (targetDomainObject is Array<*>) {
                    if (targetDomainObject.size == 2 && targetDomainObject[1]?.javaClass?.name == "kotlin.reflect.full.KCallables\$callSuspend\$1") {
                        targetDomainObject[0]
                    } else {
                        targetDomainObject
                    }
                } else {
                    targetDomainObject
                }

                return@runBlocking scopeEvaluator.evaluate(principal, adjustedTargetDomainObject, scope)
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
