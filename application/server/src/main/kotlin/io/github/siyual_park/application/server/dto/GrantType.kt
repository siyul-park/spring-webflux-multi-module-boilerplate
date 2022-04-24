package io.github.siyual_park.application.server.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(type = "String", allowableValues = ["password", "refresh_token", "client_credentials"])
enum class GrantType(val value: String) {
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials"),
}
