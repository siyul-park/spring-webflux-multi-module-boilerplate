package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import java.time.Instant

class AutoModifiable : Modifiable {
    @GeneratedValue
    override var createdAt: Instant? = null

    @GeneratedValue
    override var updatedAt: Instant? = null
}
