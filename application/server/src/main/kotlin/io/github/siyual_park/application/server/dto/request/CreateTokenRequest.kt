package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.application.server.dto.GrantType
import io.github.siyual_park.ulid.ULID
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.AssertTrue

data class CreateTokenRequest(
    @ApiModelProperty(name = "grant_type")
    val grantType: GrantType,
    @ApiModelProperty(name = "scope")
    val scope: String? = null,

    @ApiModelProperty(name = "username")
    val username: String? = null,
    @ApiModelProperty(name = "password")
    val password: String? = null,

    @ApiModelProperty(name = "refresh_token")
    val refreshToken: String? = null,

    @ApiModelProperty(name = "client_id")
    val clientId: ULID,
    @ApiModelProperty(name = "client_secret")
    val clientSecret: String? = null
) {
    @ApiModelProperty(hidden = true)
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
