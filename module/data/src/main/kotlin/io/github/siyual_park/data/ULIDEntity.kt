package io.github.siyual_park.data

import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.ulid.ULID
import org.springframework.data.annotation.Id

abstract class ULIDEntity : Entity<ULID>() {
    @Id
    @GeneratedValue
    override var id: ULID = ULID.randomULID()
}
