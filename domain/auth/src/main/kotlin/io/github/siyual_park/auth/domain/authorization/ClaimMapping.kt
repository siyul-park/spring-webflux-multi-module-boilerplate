package io.github.siyual_park.auth.domain.authorization

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ClaimMapping(
    val filterBy: KClass<*>
)
