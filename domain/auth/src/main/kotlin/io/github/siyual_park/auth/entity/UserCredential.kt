package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_credentials")
data class UserCredential(
    @Column("user_id")
    var userId: Long,

    var password: String,
    @Column("hash_algorithm")
    var hashAlgorithm: String,
) : TimeableEntity<UserCredential, Long>() {
    override fun clone(): UserCredential {
        return copyDefaultColumn(this.copy())
    }
}
