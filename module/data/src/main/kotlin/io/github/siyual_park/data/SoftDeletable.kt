package io.github.siyual_park.data

import java.time.Instant

interface SoftDeletable {
    var deletedAt: Instant?
}
