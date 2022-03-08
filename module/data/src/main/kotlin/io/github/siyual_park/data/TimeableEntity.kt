package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import java.time.Instant

abstract class TimeableEntity<T, ID> : IdEntity<T, ID>() {
    @GeneratedValue
    var createdAt: Instant? = null

    @GeneratedValue
    var updatedAt: Instant? = null
}
