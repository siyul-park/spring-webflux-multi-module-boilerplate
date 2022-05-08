package io.github.siyual_park.present.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RHSFilterParserFactory(
    private val objectMapper: ObjectMapper,
) {
    fun <T : Any> createR2dbc(clazz: KClass<T>): R2dbcRHSFilterParser<T> {
        return R2dbcRHSFilterParser(clazz, objectMapper)
    }

    fun <T : Any> createMongo(clazz: KClass<T>): MongoRHSFilterParser<T> {
        return MongoRHSFilterParser(clazz, objectMapper)
    }
}

inline fun <reified T : Any> RHSFilterParserFactory.createR2dbc(): R2dbcRHSFilterParser<T> {
    return createR2dbc(T::class)
}
inline fun <reified T : Any> RHSFilterParserFactory.createMongo(): MongoRHSFilterParser<T> {
    return createMongo(T::class)
}
