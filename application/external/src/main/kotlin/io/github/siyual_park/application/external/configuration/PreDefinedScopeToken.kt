package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.auth.entity.ScopeToken

object PreDefinedScopeToken {
    val createAccessToken = ScopeToken("access-token:create", system = true, default = true)
}
