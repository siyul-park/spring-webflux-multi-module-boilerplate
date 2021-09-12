package io.github.siyual_park.user.domain

import io.github.siyual_park.auth.domain.authenticator.AuthorizationProcessor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class BasicAuthorizationProcessor(
    private val passwordGrantAuthenticator: PasswordGrantAuthenticator,
) : AuthorizationProcessor<UserPrincipal> {
    override val type = "basic"

    override suspend fun authenticate(credentials: String): UserPrincipal? {
        val decoder = Base64.getDecoder()
        val decodedCredentials = String(decoder.decode(credentials), StandardCharsets.UTF_8)
        val token = decodedCredentials.split(":")

        if (token.size != 2) {
            return null
        }

        val payload = PasswordGrantPayload(
            username = token[0],
            password = token[1]
        )
        return passwordGrantAuthenticator.authenticate(payload)
    }
}
