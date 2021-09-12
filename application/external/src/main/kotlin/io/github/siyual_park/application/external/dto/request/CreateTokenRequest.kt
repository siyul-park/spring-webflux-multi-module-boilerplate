package io.github.siyual_park.application.external.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.siyual_park.application.external.dto.GrantType
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.AssertTrue

data class CreateTokenRequest(
    @ApiModelProperty(name = "grant_type")
    val grantType: GrantType,
    val username: String?,
    val password: String?,
    @ApiModelProperty(name = "refresh_token")
    val refreshToken: String?
) {
    @JsonIgnore
    @AssertTrue
    fun isValid(): Boolean {
        return when (grantType) {
            GrantType.PASSWORD -> username != null && password != null
            GrantType.REFRESH_TOKEN -> refreshToken != null
        }
    }
}
