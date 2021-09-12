package io.github.siyual_park.auth.domain.principal_refresher

import io.github.siyual_park.auth.domain.Principal
import kotlin.reflect.KClass

interface PrincipalRefreshProcessor<T : Principal> {
    val principalClazz: KClass<T>

    suspend fun refresh(principal: T): T
}
