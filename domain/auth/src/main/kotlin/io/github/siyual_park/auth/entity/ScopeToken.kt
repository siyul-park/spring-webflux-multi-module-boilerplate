package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.IdEntity
import io.github.siyual_park.data.copyDefaultColumn

data class ScopeToken(
    var name: String,
    var description: String,
    var system: Boolean,
    var default: Boolean
) : IdEntity<ScopeToken, Long>() {
    override fun clone(): ScopeToken {
        return copyDefaultColumn(this.copy())
    }
}
