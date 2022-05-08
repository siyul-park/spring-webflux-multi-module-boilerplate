package io.github.siyual_park.client.exception

import io.github.siyual_park.auth.exception.AuthorizeException

class SecretIncorrectException(message: String? = null) : AuthorizeException(message)
