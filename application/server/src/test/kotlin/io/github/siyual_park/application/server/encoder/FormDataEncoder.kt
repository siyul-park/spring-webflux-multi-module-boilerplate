package io.github.siyual_park.application.server.encoder

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters

@Component
class FormDataEncoder(
    private val objectMapper: ObjectMapper
) {
    fun <T> encode(data: T): BodyInserters.FormInserter<String> {
        val jsonEncode = objectMapper.writeValueAsString(data)
        val value = objectMapper.readValue(jsonEncode, Map::class.java)

        val formData = LinkedMultiValueMap<String, String>()
        value.forEach { (key, value) ->
            if (value == null) {
                return@forEach
            }

            if (value is Collection<*>) {
                value.forEach {
                    formData[key.toString()] = it.toString()
                }
            } else {
                formData[key.toString()] = value.toString()
            }
        }

        return BodyInserters.fromFormData(formData)
    }
}
