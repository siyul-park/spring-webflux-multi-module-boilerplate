package io.github.siyual_park.client.entity

import io.github.siyual_park.ulid.ULID

interface ClientAssociable {
    val clientId: ULID?
}
