package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("scope_tokens")
data class ScopeToken(
    @Key
    var name: String,
    var description: String? = null,
) : TimeableEntity<ScopeToken, Long>()

fun Collection<ScopeToken>.ids(): List<Long> {
    return this.mapNotNull { it.id }
}

fun Collection<ScopeToken>.names(): List<String> {
    return this.map { it.name }
}
