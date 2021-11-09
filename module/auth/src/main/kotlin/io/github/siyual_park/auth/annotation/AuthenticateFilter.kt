package io.github.siyual_park.auth.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AuthenticateFilter(
    val type: KClass<*>
)
