package io.github.siyual_park.auth

import io.github.siyual_park.auth.migration.CreateScopeToken
import io.github.siyual_park.auth.migration.CreateUser
import io.github.siyual_park.auth.migration.CreateUserAuthInfo
import io.github.siyual_park.auth.migration.CreateUserScope
import io.github.siyual_park.data.test.R2DBCTest

open class AuthTest : R2DBCTest() {
    init {
        migrationManager
            .register(CreateScopeToken())
            .register(CreateUser())
            .register(CreateUserAuthInfo())
            .register(CreateUserScope())
    }
}
