package io.github.siyual_park.client.entity

enum class TokenEndpointAuthMethod(val value: String) {
    NONE("none"),
    CLIENT_SECRET_POST("client_secret_post"),
    CLIENT_SECRET_BASIC("client_secret_basic"),
}
