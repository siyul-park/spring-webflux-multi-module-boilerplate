package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateStrategy
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.data.expansion.where
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.reader.finder.findByIdOrFail
import io.github.siyual_park.user.domain.UserStorage
import io.github.siyual_park.user.entity.UserData
import io.github.siyual_park.user.exception.PasswordIncorrectException
import org.springframework.stereotype.Component

@Component
@AuthenticateMapping(filterBy = PasswordGrantPayload::class)
class PasswordGrantAuthenticateStrategy(
    private val userStorage: UserStorage,
    private val clientStorage: ClientStorage
) : AuthenticateStrategy<PasswordGrantPayload, UserPrincipal> {
    override suspend fun authenticate(payload: PasswordGrantPayload): UserPrincipal? {
        val user = userStorage.loadOrFail(where(UserData::name).`is`(payload.username))
        val credential = user.getCredential()
        val client = payload.clientId?.let { clientStorage.loadOrFail(it) }

        if (!credential.checkPassword(payload.password)) {
            throw PasswordIncorrectException()
        }

        return user.toPrincipal(clientEntity = client)
    }
}
