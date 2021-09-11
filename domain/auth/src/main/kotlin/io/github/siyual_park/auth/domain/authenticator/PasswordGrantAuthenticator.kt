package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.exception.PasswordIncorrectException
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class PasswordGrantAuthenticator(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val userAuthenticationExchanger: UserAuthenticationExchanger,
) : Authenticator<PasswordGrantPayload, UserAuthentication, Long> {
    override val payloadClazz = PasswordGrantPayload::class

    override suspend fun authenticate(payload: PasswordGrantPayload): UserAuthentication {
        val user = userRepository.findByNameOrFail(payload.username)
        val userCredential = userCredentialRepository.findByUserOrFail(user)

        val messageDigest = MessageDigest.getInstance(userCredential.hashAlgorithm)
        val encodedPassword = messageDigest.hash(payload.password)

        if (userCredential.password != encodedPassword) {
            throw PasswordIncorrectException()
        }

        return userAuthenticationExchanger.exchange(user)
    }
}
