package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.IdEntity
import io.github.siyual_park.data.copyDefaultColumn

data class ScopeToken(
    var name: String,
    var description: String
) : IdEntity<ScopeToken, Long>() {
    override fun clone(): ScopeToken {
        return copyDefaultColumn(this.copy())
    }
}
