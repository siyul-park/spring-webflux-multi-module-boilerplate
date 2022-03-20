package io.github.siyual_park.auth.domain.authorization

import io.github.siyual_park.auth.domain.Principal
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AuthorizeMapping(
    val principal: KClass<out Principal>,
    val filterBy: KClass<*>
)
