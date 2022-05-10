package io.github.siyual_park.auth.domain.authentication

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = RefreshTokenPayload::class)
class RefreshTokenAuthorizationPipeline(
    private val principalRefresher: PrincipalRefresher
) : AuthenticatePipeline<Principal> {
    override val clazz = Principal::class

    override suspend fun pipe(principal: Principal): Principal {
        return principalRefresher.refresh(principal)
    }
}
