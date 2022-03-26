package io.github.siyual_park.user.exception

import io.github.siyual_park.auth.exception.AuthException

class InactivatedUserException(message: String? = null) : AuthException(message)
