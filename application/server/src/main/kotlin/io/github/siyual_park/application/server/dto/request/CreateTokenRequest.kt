package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Size

@Schema(
    type = "object",
    subTypes = [CreateTokenRequest.Password::class, CreateTokenRequest.RefreshToken::class, CreateTokenRequest.ClientCredentials::class]
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "grant_type")
@JsonSubTypes(
    JsonSubTypes.Type(name = "password", value = CreateTokenRequest.Password::class),
    JsonSubTypes.Type(name = "refresh_token", value = CreateTokenRequest.RefreshToken::class),
    JsonSubTypes.Type(name = "client_credentials", value = CreateTokenRequest.ClientCredentials::class),
)
sealed interface CreateTokenRequest {
    val scope: String?
    val clientId: ULID
    val clientSecret: String?

    data class Password(
        override val scope: String? = null,
        @field:Size(min = 3, max = 20)
        val username: String,
        @field:Size(min = 8, max = 20)
        val password: String,
        override val clientId: ULID,
        @field:Size(min = 32, max = 32)
        override val clientSecret: String? = null
    ) : CreateTokenRequest

    data class RefreshToken(
        override val scope: String? = null,
        val refreshToken: String,
        override val clientId: ULID,
        @field:Size(min = 32, max = 32)
        override val clientSecret: String? = null
    ) : CreateTokenRequest

    data class ClientCredentials(
        override val scope: String? = null,
        override val clientId: ULID,
        @field:Size(min = 32, max = 32)
        override val clientSecret: String? = null
    ) : CreateTokenRequest
}
