package io.github.siyual_park.client.exception

import io.github.siyual_park.auth.exception.AuthException

class SecretIncorrectException(message: String? = null) : AuthException(message)
