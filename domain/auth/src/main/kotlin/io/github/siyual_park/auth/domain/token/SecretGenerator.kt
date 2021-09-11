package io.github.siyual_park.auth.domain.token

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class SecretGenerator {
    fun generate(size: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(size)

        random.nextBytes(bytes)
        val encoder = Base64.getUrlEncoder().withoutPadding()
        return encoder.encodeToString(bytes)
    }
}
