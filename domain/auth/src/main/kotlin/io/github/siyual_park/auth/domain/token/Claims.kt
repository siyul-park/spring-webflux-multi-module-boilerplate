package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeToken

@Suppress("UNCHECKED_CAST")
class Claims : MutableMap<String, Any> by mutableMapOf() {
    init {
        put("scope", emptySet<ScopeToken>())
    }

    var scope: Collection<ScopeToken>
        get() = get("scope") as Collection<ScopeToken>
        set(value) { put("scope", value) }
}
