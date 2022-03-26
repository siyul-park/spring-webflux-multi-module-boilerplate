package io.github.siyual_park.user.event

import io.github.siyual_park.user.domain.User

data class RequestActivateEvent(
    val entity: User
)
