package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.user.entity.UserCredentialData
import io.github.siyual_park.user.repository.UserCredentialRepository
import java.security.MessageDigest

class UserCredential(
    value: UserCredentialData,
    userCredentialRepository: UserCredentialRepository,
    eventPublisher: EventPublisher
) : Persistence<UserCredentialData, Long>(value, userCredentialRepository, eventPublisher) {
    val id: Long?
        get() = root[UserCredentialData::id]

    val userId: Long
        get() = root[UserCredentialData::userId]

    var hashAlgorithm: String
        get() = root[UserCredentialData::hashAlgorithm]
        set(value) { root[UserCredentialData::hashAlgorithm] = value }

    fun checkPassword(password: String): Boolean {
        return root[UserCredentialData::password] == encodePassword(password)
    }

    fun setPassword(password: String) {
        root[UserCredentialData::password] = encodePassword(password)
    }

    private fun encodePassword(password: String): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        return messageDigest.hash(password)
    }
}
