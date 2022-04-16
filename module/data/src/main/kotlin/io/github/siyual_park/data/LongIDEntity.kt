package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import org.springframework.data.annotation.Id

abstract class LongIDEntity : Entity<Long?>() {
    @Id
    @GeneratedValue
    override var id: Long? = null
}
