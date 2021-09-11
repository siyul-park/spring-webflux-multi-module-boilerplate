package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("scope_tokens")
data class ScopeToken(
    var name: String,
    var descridescriptionption: String? = null,
    var system: Boolean,
    var default: Boolean = false
) : TimeableEntity<ScopeToken, Long>() {
    override fun clone(): ScopeToken {
        return copyDefaultColumn(this.copy())
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}
