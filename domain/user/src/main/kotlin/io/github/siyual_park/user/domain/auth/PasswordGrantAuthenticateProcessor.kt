package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthenticateProcessor
import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.search.finder.findByIdOrFail
import io.github.siyual_park.user.domain.UserFinder
import io.github.siyual_park.user.exception.PasswordIncorrectException
import io.github.siyual_park.user.repository.UserCredentialRepository
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
@AuthenticateMapping(filterBy = PasswordGrantPayload::class)
class PasswordGrantAuthenticateProcessor(
    private val userFinder: UserFinder,
    private val clientFinder: ClientFinder,
    private val userCredentialRepository: UserCredentialRepository,
    private val userPrincipalExchanger: UserPrincipalExchanger,
) : AuthenticateProcessor<PasswordGrantPayload, UserPrincipal> {
    override suspend fun authenticate(payload: PasswordGrantPayload): UserPrincipal? {
        val user = userFinder.findByNameOrFail(payload.username)
        val client = payload.clientId?.let { clientFinder.findByIdOrFail(it) }
        val userCredential = userCredentialRepository.findByUserOrFail(user)

        val messageDigest = MessageDigest.getInstance(userCredential.hashAlgorithm)
        val encodedPassword = messageDigest.hash(payload.password)

        if (userCredential.password != encodedPassword) {
            throw PasswordIncorrectException()
        }

        return userPrincipalExchanger.exchange(user, client)
    }
}
