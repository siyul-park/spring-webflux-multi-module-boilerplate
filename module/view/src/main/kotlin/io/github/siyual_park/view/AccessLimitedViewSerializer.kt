package io.github.siyual_park.view

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.stereotype.Component

@Component
class AccessLimitedViewSerializer(
    private val objectMapper: ObjectMapper
) : JsonSerializer<AccessLimiter<*>>() {
    override fun serialize(value: AccessLimiter<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        val jsonNode = objectMapper.readTree(
            if (value.level == null) {
                objectMapper.writeValueAsString(value.value)
            } else {
                objectMapper.writerWithView(value.level.java).writeValueAsString(value.value)
            }
        )
        gen.writeObject(jsonNode)
    }
}
