package io.github.siyual_park.validation.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateMapping(
    val annotation: KClass<out Annotation>
)
