package io.github.siyual_park.user.exception

import io.github.siyual_park.auth.exception.AuthorizeException

class IncorrectPasswordException(message: String? = null) : AuthorizeException(message)
