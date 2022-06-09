package io.github.siyual_park.auth.domain

import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class ScopeEvaluator(
    private val scopeTokenStorage: ScopeTokenStorage,
    private val authorizator: Authorizator,
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any): Boolean {
        val principal = authentication.principal
        if (principal !is Principal) {
            return false
        }

        try {
            val scope = getScope(permission)?.let {
                when (it) {
                    is Collection<*> -> it
                    is ScopeToken -> listOf(it)
                    else -> null
                }
            }?.toList() ?: return false
            val adjustTargetDomainObject = (
                if (targetDomainObject == null) {
                    null
                } else {
                    if (permission is Collection<*>) {
                        (targetDomainObject as? Collection<Any?>)?.toList() ?: return false
                    } else {
                        listOf(targetDomainObject)
                    }
                }
                )?.map { adjustTargetDomainObject(it) }

            return runBlocking { authorizator.authorize(principal, scope, adjustTargetDomainObject) }
        } catch (e: Exception) {
            return false
        }
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable?,
        targetType: String?,
        permission: Any
    ): Boolean {
        return false
    }

    private fun getScope(permission: Any?): Any? {
        return when (permission) {
            is String -> {
                runBlocking { scopeTokenStorage.loadOrFail(permission) }
            }
            is Collection<*> -> {
                permission.map { getScope(it) }.toList()
            }
            else -> {
                null
            }
        }
    }

    private fun adjustTargetDomainObject(targetDomainObject: Any?): Any? {
        return if (targetDomainObject is Array<*>) {
            if (targetDomainObject.size == 2 && targetDomainObject[1]?.javaClass?.name == "kotlin.reflect.full.KCallables\$callSuspend\$1") {
                targetDomainObject[0]
            } else {
                targetDomainObject
            }
        } else {
            targetDomainObject
        }
    }
}
