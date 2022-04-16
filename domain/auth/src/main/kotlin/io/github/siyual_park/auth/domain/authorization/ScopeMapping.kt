package io.github.siyual_park.auth.domain.authorization

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ScopeMapping(
    val scope: Array<String>
)
