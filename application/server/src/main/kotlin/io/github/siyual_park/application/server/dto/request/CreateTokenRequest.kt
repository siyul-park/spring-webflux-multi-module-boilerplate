package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.AssertTrue

data class CreateTokenRequest(
    @Schema(name = "grant_type")
    val grantType: GrantType,
    @Schema(name = "scope")
    val scope: String? = null,

    @Schema(name = "username")
    val username: String? = null,
    @Schema(name = "password")
    val password: String? = null,

    @Schema(name = "refresh_token")
    val refreshToken: String? = null,

    @Schema(name = "client_id")
    val clientId: ULID,
    @Schema(name = "client_secret")
    val clientSecret: String? = null
) {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonIgnore
    @AssertTrue
    fun isValid(): Boolean {
        return when (grantType) {
            GrantType.PASSWORD -> username != null && password != null
            GrantType.REFRESH_TOKEN -> refreshToken != null
            GrantType.CLIENT_CREDENTIALS -> true
        }
    }
}
