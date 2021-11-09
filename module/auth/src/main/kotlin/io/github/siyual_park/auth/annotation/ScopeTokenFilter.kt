package io.github.siyual_park.auth.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ScopeTokenFilter(
    val names: Array<String>
)
