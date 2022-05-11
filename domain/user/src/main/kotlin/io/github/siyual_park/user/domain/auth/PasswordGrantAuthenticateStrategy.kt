package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.user.domain.UserStorage
import io.github.siyual_park.user.domain.loadOrFail
import io.github.siyual_park.user.exception.IncorrectPasswordException
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping
class PasswordGrantAuthenticateStrategy(
    private val userStorage: UserStorage,
    private val clientStorage: ClientStorage
) : AuthenticateStrategy<PasswordGrantPayload, UserPrincipal> {
    override val clazz = PasswordGrantPayload::class

    override suspend fun authenticate(payload: PasswordGrantPayload): UserPrincipal? {
        val user = userStorage.loadOrFail(payload.username)
        val credential = user.getCredential()
        val client = payload.clientId?.let { clientStorage.loadOrFail(it) }

        if (!credential.isPassword(payload.password)) {
            throw IncorrectPasswordException()
        }

        return user.toPrincipal(clientEntity = client)
    }
}
