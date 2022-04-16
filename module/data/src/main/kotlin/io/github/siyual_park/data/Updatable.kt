package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import java.time.Instant

interface Updatable {
    @GeneratedValue
    var updatedAt: Instant?
}
