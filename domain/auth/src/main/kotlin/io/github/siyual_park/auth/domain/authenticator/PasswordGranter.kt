package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.ScopeFinder
import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.auth.exception.PasswordIncorrectException
import io.github.siyual_park.auth.repository.UserCredentialRepository
import io.github.siyual_park.auth.repository.UserRepository
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class PasswordGranter(
    private val userRepository: UserRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val scopeFinder: ScopeFinder,
) : Authenticator<PasswordGrantInfo, UserAuthentication, Long> {
    override val infoClazz = PasswordGrantInfo::class

    override suspend fun authenticate(info: PasswordGrantInfo): UserAuthentication {
        val user = userRepository.findByNameOrFail(info.username)
        val userCredential = userCredentialRepository.findByUserOrFail(user)

        val messageDigest = MessageDigest.getInstance(userCredential.hashAlgorithm)
        val encodedPassword = messageDigest.hash(info.password)

        if (userCredential.password != encodedPassword) {
            throw PasswordIncorrectException()
        }

        val scope = scopeFinder.find(user)

        return UserAuthentication(
            id = user.id!!,
            scope = scope.toSet()
        )
    }
}
