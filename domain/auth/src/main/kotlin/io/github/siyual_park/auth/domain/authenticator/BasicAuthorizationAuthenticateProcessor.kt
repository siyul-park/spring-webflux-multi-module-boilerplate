package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.exception.InvalidAuthorizationFormatException
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class BasicAuthorizationAuthenticateProcessor(
    private val passwordGrantAuthenticator: PasswordGrantAuthenticator,
) : AuthorizationAuthenticateProcessor<UserPrincipal> {
    override val type = "basic"

    override suspend fun authenticate(credentials: String): UserPrincipal {
        val decoder = Base64.getDecoder()
        val decodedCredentials = decoder.decode(credentials).toString()
        val token = decodedCredentials.split(":")

        if (token.size != 2) {
            throw InvalidAuthorizationFormatException()
        }

        val payload = PasswordGrantPayload(
            username = token[0],
            password = token[1]
        )
        return passwordGrantAuthenticator.authenticate(payload)
    }
}
