package io.github.siyual_park.auth.domain.authenticator

import kotlin.reflect.KClass

interface Authenticator<INFO : AuthenticationInfo, AUTHENTICATION : Authentication<ID>, ID> {
    val infoClazz: KClass<INFO>

    suspend fun authenticate(info: INFO): AUTHENTICATION
}
