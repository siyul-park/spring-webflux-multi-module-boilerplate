package io.github.siyual_park.auth.spring

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFinder
import io.github.siyual_park.auth.entity.ScopeToken
import kotlinx.coroutines.runBlocking
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
class ScopeEvaluator(
    private val scopeTokenFinder: ScopeTokenFinder,
    private val authorizator: Authorizator,
) : PermissionEvaluator {
    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any): Boolean {
        return runBlocking {
            val principal = authentication.principal
            if (principal !is Principal) {
                return@runBlocking false
            }

            try {
                val scope = getScope(permission) ?: return@runBlocking false
                val adjustTargetDomainObject = (
                    if (targetDomainObject == null) {
                        null
                    } else {
                        if (permission is Collection<*>) {
                            (targetDomainObject as? Collection<Any?>)?.toList() ?: return@runBlocking false
                        } else {
                            listOf(targetDomainObject)
                        }
                    }
                    )?.map { adjustTargetDomainObject(it) }

                return@runBlocking authorizator.authorize(principal, scope, adjustTargetDomainObject)
            } catch (e: Exception) {
                return@runBlocking false
            }
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

    private suspend fun getScope(permission: Any): List<ScopeToken>? {
        return when (permission) {
            is String -> {
                listOf(scopeTokenFinder.findByNameOrFail(permission))
            }
            is Collection<*> -> {
                permission.map { scopeTokenFinder.findByNameOrFail(it.toString()) }.toList()
            }
            else -> {
                null
            }
        }
    }

    private suspend fun adjustTargetDomainObject(targetDomainObject: Any?): Any? {
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
