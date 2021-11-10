package io.github.siyual_park.user.domain.auth

import io.github.siyual_park.auth.domain.authentication.AuthenticateMapping
import io.github.siyual_park.auth.domain.authentication.AuthorizationPayload
import io.github.siyual_park.auth.domain.authentication.AuthorizationProcessor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
@AuthenticateMapping(filterBy = AuthorizationPayload::class)
class BasicAuthorizationProcessor(
    private val passwordGrantAuthenticator: PasswordGrantAuthenticateProcessor,
) : AuthorizationProcessor<UserPrincipal>("basic") {
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

        return try {
            passwordGrantAuthenticator.authenticate(payload)
        } catch (e: Exception) {
            null
        }
    }
}
