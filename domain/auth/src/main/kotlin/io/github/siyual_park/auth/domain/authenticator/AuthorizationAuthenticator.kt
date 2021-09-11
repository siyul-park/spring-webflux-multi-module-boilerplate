package io.github.siyual_park.auth.domain.authenticator

import io.github.siyual_park.auth.domain.token.TokenExchanger
import io.github.siyual_park.auth.exception.InvalidAuthorizationFormatException
import io.github.siyual_park.auth.exception.UnsupportedAuthorizationTypeException
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class AuthorizationAuthenticator(
    private val passwordGrantAuthenticator: PasswordGrantAuthenticator,
    private val tokenExchanger: TokenExchanger
) : Authenticator<AuthorizationPayload, Principal<Long>, Long> {
    override val payloadClazz = AuthorizationPayload::class

    override suspend fun authenticate(payload: AuthorizationPayload): Principal<Long> {
        return when (payload.type.lowercase()) {
            "basic" -> basicAuthenticate(payload.credentials)
            "bearer" -> bearerAuthenticate(payload.credentials)
            else -> throw UnsupportedAuthorizationTypeException()
        }
    }

    private suspend fun basicAuthenticate(credentials: String): UserPrincipal {
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

    private fun bearerAuthenticate(credentials: String): UserPrincipal {
        val authentication = tokenExchanger.decode(credentials)
        return UserPrincipal(
            id = authentication.id.toLong(),
            scope = authentication.scope
        )
    }
}
