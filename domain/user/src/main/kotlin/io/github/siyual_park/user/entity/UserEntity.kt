package io.github.siyual_park.user.entity

import io.github.siyual_park.ulid.ULID

interface UserEntity {
    val userId: ULID?
}
