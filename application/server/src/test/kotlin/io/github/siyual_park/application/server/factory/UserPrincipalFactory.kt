package io.github.siyual_park.application.server.factory

import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import org.springframework.stereotype.Component

@Component
class UserPrincipalFactory(
    private val userFactory: UserFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
) {
    private val createUserPayloadFactory = CreateUserPayloadFactory()

    suspend fun create(): UserPrincipal {
        return createUserPayloadFactory.create()
            .let { userFactory.create(it) }
            .let { userPrincipalExchanger.exchange(it) }
    }
}
