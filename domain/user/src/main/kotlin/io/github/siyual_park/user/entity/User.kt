package io.github.siyual_park.user.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class User(
    var name: String,
    @GeneratedValue
    var deletedAt: Instant? = null
) : TimeableEntity<User, Long>(), UserEntity {
    override fun clone(): User {
        return copyDefaultColumn(this.copy())
    }

    override val userId: Long?
        get() = id
}
