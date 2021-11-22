package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.data.patch.Patch
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class PatchExchanger(
    private val objectMapper: ObjectMapper
) {
    fun <IN : Any, OUT : Any> exchange(
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

inline fun <reified IN : Any, reified OUT : Any> PatchExchanger.exchange(patch: Patch<IN>) = exchange(
    patch,
    IN::class,
    OUT::class
)
