package io.github.siyual_park.user.entity

import io.github.siyual_park.ulid.ULID

interface UserAssociable {
    val userId: ULID?
}
