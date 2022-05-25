package io.github.siyual_park.presentation.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RHSFilterParserFactory(
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> create(clazz: KClass<T>): RHSFilterParser<T> {
        return RHSFilterParser(clazz, objectMapper)
    }
}

inline fun <reified T : Any> RHSFilterParserFactory.create(): RHSFilterParser<T> {
    return this.create(T::class)
}
