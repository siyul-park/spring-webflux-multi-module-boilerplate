package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.patch.Patch
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class PatchConverter(
    private val objectMapper: ObjectMapper
) {
    fun <IN : Any, OUT : Any> convert(
        patch: Patch<IN>,
        source: KClass<IN>,
        target: KClass<OUT>
    ): Patch<OUT> {
        return Patch.from {
            val internal = objectMapper.convertValue(it, source.java)
            val result = patch.apply(internal)
            objectMapper.updateValue(it, result)
        }
    }
}

inline fun <reified IN : Any, reified OUT : Any> PatchConverter.convert(patch: Patch<IN>) = convert(
    patch,
    IN::class,
    OUT::class
)
