package io.github.siyual_park.auth.domain.principal_refresher

import io.github.siyual_park.auth.domain.Principal

interface PrincipalRefreshStrategy<T : Principal> {
    suspend fun refresh(principal: T): T
}
