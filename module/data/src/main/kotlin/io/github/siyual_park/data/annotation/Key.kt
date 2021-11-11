package io.github.siyual_park.data.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Key(
    val name: String = ""
)
