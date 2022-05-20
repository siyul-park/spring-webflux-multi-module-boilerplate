package io.github.siyual_park.presentation.project

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ProjectionParserFactory(
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> create(clazz: KClass<T>): ProjectionParser<T> {
        return ProjectionParser(clazz, objectMapper)
    }
}

inline fun <reified T : Any> ProjectionParserFactory.create(): ProjectionParser<T> {
    return create(T::class)
}
