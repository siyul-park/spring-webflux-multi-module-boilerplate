package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.application.server.dto.GrantType
import javax.validation.constraints.AssertTrue

data class CreateTokenRequest(
    val grantType: GrantType,
    val scope: String? = null,

    val username: String? = null,
    val password: String? = null,

    val refreshToken: String? = null,

    val clientId: Long,
    val clientSecret: String? = null
) {
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
