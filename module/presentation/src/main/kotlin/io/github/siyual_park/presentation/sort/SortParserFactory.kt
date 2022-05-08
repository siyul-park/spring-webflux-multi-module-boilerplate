package io.github.siyual_park.presentation.sort

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class SortParserFactory(
    private val objectMapper: ObjectMapper
) {
    fun <T : Any> create(clazz: KClass<T>): SortParser<T> {
        return SortParser(clazz, objectMapper)
    }
}

inline fun <reified T : Any> SortParserFactory.create(): SortParser<T> {
    return create(T::class)
}
