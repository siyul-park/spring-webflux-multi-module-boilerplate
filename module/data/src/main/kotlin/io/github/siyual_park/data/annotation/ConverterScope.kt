package io.github.siyual_park.data.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterScope(
    val type: Type
) {
    enum class Type {
        MONGO,
        R2DBC
    }
}
