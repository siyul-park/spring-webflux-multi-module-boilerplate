package io.github.siyual_park.application.server.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.application.server.dto.GrantType
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.AssertTrue

data class CreateTokenRequest(
    @ApiModelProperty(name = "grant_type")
    val grantType: GrantType,
    val username: String? = null,
    val password: String? = null,
    @ApiModelProperty(name = "refresh_token")
    val refreshToken: String? = null,
    @ApiModelProperty(name = "client_id")
    val clientId: Long,
    @ApiModelProperty(name = "client_secret")
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
