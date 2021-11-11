package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("scope_tokens")
data class ScopeToken(
    @Key
    var name: String,
    var description: String? = null,
) : TimeableEntity<ScopeToken, Long>() {
    override fun clone(): ScopeToken {
        return copyDefaultColumn(this.copy())
    }
}

fun Collection<ScopeToken>.ids(): List<Long> {
    return this.mapNotNull { it.id }
}

fun Collection<ScopeToken>.names(): List<String> {
    return this.map { it.name }
}
