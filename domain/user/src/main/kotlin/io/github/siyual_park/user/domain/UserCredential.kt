package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.hash
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull
import io.github.siyual_park.user.entity.UserCredentialData
import io.github.siyual_park.user.repository.UserCredentialDataRepository
import java.security.MessageDigest

class UserCredential(
    value: UserCredentialData,
    userCredentialDataRepository: UserCredentialDataRepository
) : Persistence<UserCredentialData, Long>(value, userCredentialDataRepository) {
    val id by proxyNotNull(root, UserCredentialData::id)
    val userId by proxy(root, UserCredentialData::userId)

    val createdAt by proxy(root, UserCredentialData::createdAt)
    val updatedAt by proxy(root, UserCredentialData::updatedAt)

    private val hashAlgorithm by proxy(root, UserCredentialData::hashAlgorithm)

    fun check(password: String): Boolean {
        return root[UserCredentialData::password] == encodePassword(password)
    }

    fun set(password: String) {
        root[UserCredentialData::password] = encodePassword(password)
    }

    private fun encodePassword(password: String): String {
        val messageDigest = MessageDigest.getInstance(hashAlgorithm)
        return messageDigest.hash(password)
    }
}
