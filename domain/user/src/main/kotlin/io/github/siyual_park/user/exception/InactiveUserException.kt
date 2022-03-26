package io.github.siyual_park.user.exception

import io.github.siyual_park.auth.exception.AuthException

class InactiveUserException(message: String? = null) : AuthException(message)
